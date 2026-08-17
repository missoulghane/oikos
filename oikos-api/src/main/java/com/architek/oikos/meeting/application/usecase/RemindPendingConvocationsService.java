package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.RemindPendingConvocationsCommand;
import com.architek.oikos.meeting.application.port.in.RemindPendingConvocationsUseCase;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Chases the lots that were reached and have still not answered
 * (Convocation.awaitsReply). Deliberately not the ones whose convocation never
 * went out: reminding someone of a letter they never received is noise, and
 * those lots need sending, not chasing.
 *
 * <p>Reminders carry no PDF: the convocation was already sent and its document
 * is already filed, so a reminder is a chase, not a second convocation.
 *
 * <p>They do, however, leave a trace. This service used to send its emails and
 * write nothing at all - it was even declared readOnly - so three reminders were
 * indistinguishable from none in the tracking table, which is exactly what a
 * syndic has to be able to show on a contested AG. Each attempt is now recorded
 * as a ConvocationDelivery like any other send, flagged as a reminder purely so
 * the screen can label it.
 *
 * <p>Recording them changes nothing that is computed. sentAt keeps naming the
 * FIRST successful attempt - the date the notice period runs from - and the
 * derived delivery status ignores the flag entirely. A reminder can neither
 * push that date back nor turn a failed convocation into a sent one, since only
 * lots already reached are chased in the first place.
 */
@Component
public class RemindPendingConvocationsService implements RemindPendingConvocationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RemindPendingConvocationsService.class);

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler viewAssembler;
    private final ConvocationEmailComposer emailComposer;
    private final ConvocationLinkComposer linkComposer;
    private final EmailSenderPort emailSenderPort;
    private final Clock clock;

    public RemindPendingConvocationsService(GeneralMeetingRepository generalMeetingRepository,
                                             ConvocationRepository convocationRepository,
                                             PropertyDirectoryPort propertyDirectoryPort,
                                             ConvocationViewAssembler viewAssembler,
                                             ConvocationEmailComposer emailComposer,
                                             ConvocationLinkComposer linkComposer,
                                             EmailSenderPort emailSenderPort, Clock clock) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
        this.emailComposer = emailComposer;
        this.linkComposer = linkComposer;
        this.emailSenderPort = emailSenderPort;
        this.clock = clock;
    }

    /**
     * Appends one attempt to the convocation and saves it. Saved per lot rather
     * than collected and flushed at the end: the loop below deliberately
     * swallows a failing send so the rest of the run goes out, and a batch
     * written only at the end would lose every attempt recorded before the one
     * that threw somewhere else.
     */
    private void record(Convocation convocation, ConvocationDelivery delivery) {
        convocationRepository.save(convocation.recordDelivery(delivery));
    }

    @Override
    @Transactional
    public int remind(RemindPendingConvocationsCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        Map<EntityId, UnitInfo> unitsById = viewAssembler.unitsByIdOf(meeting.getPropertyId());
        String propertyName = propertyDirectoryPort.getProperty(meeting.getPropertyId()).name();
        String subject = emailComposer.reminderSubject(propertyName, meeting);

        int reminded = 0;
        for (Convocation convocation : convocationRepository.findByGeneralMeetingId(meeting.getId())) {
            if (!convocation.awaitsReply()) {
                continue;
            }
            UnitInfo unit = unitsById.get(convocation.getUnitId());
            List<String> emails = unit == null ? List.of()
                    : unit.owners().stream().map(OwnerInfo::email).filter(email -> email != null && !email.isBlank()).toList();
            if (emails.isEmpty()) {
                // Recorded rather than skipped, exactly as SendConvocationService records a lot it
                // cannot email: "we tried to chase this one and it has no address" is what tells the
                // syndic to pick up the phone instead, and silence says nothing at all.
                record(convocation, ConvocationDelivery.reminderFailed(ConvocationDeliveryId.newId(),
                        ChannelCode.EMAIL, clock.instant(), command.requestedByUserId()));
                continue;
            }
            // The reminder carries the link too: a lot that has not answered is very often one
            // whose owners had no way to.
            String body = emailComposer.reminderHtmlBody(propertyName, meeting, unit,
                    linkComposer.link(convocation.getConfirmationToken()));
            // One failure must not stop the run - a reminder is best effort by nature, and the
            // tracking table now carries the failure itself rather than only the silence.
            try {
                for (String email : emails) {
                    emailSenderPort.send(EmailVO.of(email), subject, body);
                }
                record(convocation, ConvocationDelivery.reminderSent(ConvocationDeliveryId.newId(), ChannelCode.EMAIL,
                        clock.instant(), command.requestedByUserId()));
                reminded++;
            } catch (RuntimeException e) {
                log.warn("Reminder failed for convocation {}", convocation.getId(), e);
                record(convocation, ConvocationDelivery.reminderFailed(ConvocationDeliveryId.newId(),
                        ChannelCode.EMAIL, clock.instant(), command.requestedByUserId()));
            }
        }
        log.info("{} reminder(s) sent for general meeting {}", reminded, meeting.getId());
        return reminded;
    }
}
