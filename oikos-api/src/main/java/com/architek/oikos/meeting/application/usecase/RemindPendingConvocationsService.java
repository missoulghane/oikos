package com.architek.oikos.meeting.application.usecase;

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
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Chases the lots that were reached and have still not answered
 * (Convocation.awaitsReply). Deliberately not the ones whose convocation never
 * went out: reminding someone of a letter they never received is noise, and
 * those lots need sending, not chasing.
 *
 * <p>Reminders carry no PDF and change no state: the convocation was already
 * sent, its document is already filed, and a reminder is not a second
 * convocation. Nothing here rewrites sentAt, which must keep naming the date
 * the convocation itself went out - that date is what a contested AG turns on.
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

    public RemindPendingConvocationsService(GeneralMeetingRepository generalMeetingRepository,
                                             ConvocationRepository convocationRepository,
                                             PropertyDirectoryPort propertyDirectoryPort,
                                             ConvocationViewAssembler viewAssembler,
                                             ConvocationEmailComposer emailComposer,
                                             ConvocationLinkComposer linkComposer,
                                             EmailSenderPort emailSenderPort) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
        this.emailComposer = emailComposer;
        this.linkComposer = linkComposer;
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    @Transactional(readOnly = true)
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
                continue;
            }
            // The reminder carries the link too: a lot that has not answered is very often one
            // whose owners had no way to.
            String body = emailComposer.reminderHtmlBody(propertyName, meeting, unit,
                    linkComposer.link(convocation.getConfirmationToken()));
            // One failure must not stop the run - a reminder is best effort by nature, and the
            // tracking table already tells the syndic who is still silent.
            try {
                for (String email : emails) {
                    emailSenderPort.send(EmailVO.of(email), subject, body);
                }
                reminded++;
            } catch (RuntimeException e) {
                log.warn("Reminder failed for convocation {}", convocation.getId(), e);
            }
        }
        log.info("{} reminder(s) sent for general meeting {}", reminded, meeting.getId());
        return reminded;
    }
}
