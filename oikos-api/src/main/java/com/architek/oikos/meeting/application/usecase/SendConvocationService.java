package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.SendConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.SendConvocationUseCase;
import com.architek.oikos.meeting.application.port.out.ConvocationDocumentPort;
import com.architek.oikos.meeting.application.port.out.ConvocationRendererPort;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.ConvocationNotFoundException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.NoConvocationRecipientException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Sends one lot's convocation: renders the letter, files it as a Document
 * attached to the convocation, emails it to the lot's current owners, and
 * notifies those who have an account.
 *
 * <p>The owners are resolved here, at send time, rather than stored on the
 * convocation - a sale between the convocation and the session then needs no
 * migration, and the letter goes to whoever owns the lot now.
 *
 * <p>Only the automated channels reach this method, and only those an emitter
 * exists for. A manual channel is refused outright: it is recorded through
 * RecordConvocationDeliveryUseCase once a person has actually posted something,
 * because pressing a button must never be able to claim a letter left the
 * building. A channel merely flagged automated in the catalog is refused too -
 * since the channels became data, a row can promise an automation nobody wrote,
 * and failing here is what keeps that promise from being silently broken.
 *
 * <p>The PDF is filed whatever happens to the email. A convocation that could
 * not be delivered still has to exist as a document - it is what the syndic
 * prints and posts.
 */
@Component
public class SendConvocationService implements SendConvocationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendConvocationService.class);

    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler viewAssembler;
    private final ConvocationChannelLookup channelLookup;
    private final ConvocationDocumentComposer documentComposer;
    private final ConvocationRendererPort rendererPort;
    private final ConvocationDocumentPort documentPort;
    private final ConvocationEmailComposer emailComposer;
    private final ConvocationLinkComposer linkComposer;
    private final EmailSenderPort emailSenderPort;
    private final MeetingNotificationDispatcher notificationDispatcher;
    private final Clock clock;

    public SendConvocationService(ConvocationRepository convocationRepository,
                                   GeneralMeetingRepository generalMeetingRepository,
                                   PropertyDirectoryPort propertyDirectoryPort,
                                   ConvocationViewAssembler viewAssembler, ConvocationChannelLookup channelLookup,
                                   ConvocationDocumentComposer documentComposer, ConvocationRendererPort rendererPort,
                                   ConvocationDocumentPort documentPort, ConvocationEmailComposer emailComposer,
                                   ConvocationLinkComposer linkComposer,
                                   EmailSenderPort emailSenderPort,
                                   MeetingNotificationDispatcher notificationDispatcher, Clock clock) {
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
        this.channelLookup = channelLookup;
        this.documentComposer = documentComposer;
        this.rendererPort = rendererPort;
        this.documentPort = documentPort;
        this.emailComposer = emailComposer;
        this.linkComposer = linkComposer;
        this.emailSenderPort = emailSenderPort;
        this.notificationDispatcher = notificationDispatcher;
        this.clock = clock;
    }

    /**
     * REQUIRES_NEW, and not the default REQUIRED: the bulk path calls this from
     * an AFTER_COMMIT listener, where the original transaction is completing but
     * its synchronization is still bound to the thread. A REQUIRED transaction
     * joins that dying context and every write here is discarded on the way out,
     * silently - the same trap GeneratePaymentReceiptService documents, and the
     * tracking table would then show TO_SEND for convocations that really went
     * out.
     *
     * <p>noRollbackFor is the other half of that: an unreachable lot is recorded
     * as FAILED and then reported as an error, and without this the rollback
     * triggered by that very exception would erase the record on its way out.
     * The row would go back to TO_SEND - indistinguishable from a lot nobody has
     * tried yet, which is precisely the distinction the syndic needs to convoke
     * it by post instead.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoConvocationRecipientException.class)
    public ConvocationView send(SendConvocationCommand command) {
        channelLookup.requireSendable(command.channel());
        Convocation convocation = convocationRepository.findById(command.convocationId())
                .orElseThrow(() -> new ConvocationNotFoundException(command.convocationId()));
        GeneralMeeting meeting = generalMeetingRepository.findById(convocation.getGeneralMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(convocation.getGeneralMeetingId()));

        Map<EntityId, UnitInfo> unitsById = viewAssembler.unitsByIdOf(meeting.getPropertyId());
        UnitInfo unit = unitsById.get(convocation.getUnitId());
        String propertyName = propertyDirectoryPort.getProperty(meeting.getPropertyId()).name();

        // Re-rendered rather than reusing whatever was filed at generation time: the meeting's
        // date and venue stay editable (ADR 0002 §8), so what goes out has to be current.
        byte[] pdf = rendererPort.render(documentComposer.compose(convocation, meeting, unit, propertyName));
        documentPort.replaceConvocationDocument(convocation.getId(), ConvocationDocumentComposer.fileNameFor(unit),
                pdf, command.requestedByUserId());

        List<OwnerInfo> recipients = unit == null ? List.of() : unit.owners();
        List<String> emails = recipients.stream().map(OwnerInfo::email).filter(email -> email != null && !email.isBlank())
                .toList();
        if (emails.isEmpty()) {
            // Recorded as FAILED rather than left pending: an unreachable lot is exactly what the
            // syndic must see in the tracking table to convoke it by post instead.
            Convocation failed = convocation.recordDelivery(ConvocationDelivery.failed(ConvocationDeliveryId.newId(),
                    command.channel(), clock.instant(), command.requestedByUserId()));
            ConvocationView view = viewAssembler.toView(convocationRepository.save(failed), unit);
            throw new NoConvocationRecipientException(view.unitNumber());
        }

        String subject = emailComposer.subject(propertyName, meeting);
        String body = emailComposer.htmlBody(propertyName, meeting, unit,
                linkComposer.link(convocation.getConfirmationToken()));
        for (String email : emails) {
            emailSenderPort.send(EmailVO.of(email), subject, body);
        }

        notifyOwnersWithAnAccount(recipients, meeting, propertyName);

        Convocation sent = convocation.recordDelivery(ConvocationDelivery.sent(ConvocationDeliveryId.newId(),
                command.channel(), clock.instant(), null, command.requestedByUserId()));
        log.info("Convocation {} sent to {} recipient(s) by {}", convocation.getId(), emails.size(), command.channel());
        return viewAssembler.toView(convocationRepository.save(sent), unit);
    }

    /**
     * In-app notification on top of the email, for the owners who have an
     * account. Best effort by design: not being notified in the app must never
     * make a convocation fail, the email and the filed PDF are the record.
     *
     * <p>Delegated to MeetingNotificationDispatcher rather than done here, and
     * that indirection is the whole point: its REQUIRES_NEW suspends this
     * transaction, so a failure marks that one instead of this one. Inline, the
     * catch below was decorative - the notification's own services join this
     * transaction, mark it rollback-only on their way out, and the commit failed
     * afterwards whatever was caught.
     */
    private void notifyOwnersWithAnAccount(List<OwnerInfo> owners, GeneralMeeting meeting, String propertyName) {
        if (owners.isEmpty()) {
            return;
        }
        try {
            notificationDispatcher.notifyConvoked(owners.stream().map(OwnerInfo::partyId).toList(), meeting);
        } catch (RuntimeException e) {
            log.warn("In-app notification failed for general meeting {} on property {} - the convocation itself stands",
                    meeting.getId(), propertyName, e);
        }
    }

}
