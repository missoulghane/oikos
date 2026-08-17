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
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Sends one lot's convocation on ONE channel: renders the letter, files it as a
 * Document attached to the convocation, then either emails the lot's current
 * owners or drops the convocation into the messagerie of those who have an
 * account.
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
    private final MeetingInAppDispatcher dispatcher;
    private final Clock clock;

    public SendConvocationService(ConvocationRepository convocationRepository,
                                   GeneralMeetingRepository generalMeetingRepository,
                                   PropertyDirectoryPort propertyDirectoryPort,
                                   ConvocationViewAssembler viewAssembler, ConvocationChannelLookup channelLookup,
                                   ConvocationDocumentComposer documentComposer, ConvocationRendererPort rendererPort,
                                   ConvocationDocumentPort documentPort, ConvocationEmailComposer emailComposer,
                                   ConvocationLinkComposer linkComposer,
                                   EmailSenderPort emailSenderPort,
                                   MeetingInAppDispatcher dispatcher, Clock clock) {
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
        this.dispatcher = dispatcher;
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
        // Filed whatever the channel and whatever becomes of the send: a convocation that could
        // not be delivered still has to exist as a document, since it is what the syndic prints.
        byte[] pdf = rendererPort.render(documentComposer.compose(convocation, meeting, unit, propertyName));
        documentPort.replaceConvocationDocument(convocation.getId(), ConvocationDocumentComposer.fileNameFor(unit),
                pdf, command.requestedByUserId());

        List<OwnerInfo> recipients = unit == null ? List.of() : unit.owners();
        if (!emit(command.channel(), convocation, meeting, unit, propertyName, recipients,
                command.requestedByUserId())) {
            // Recorded as FAILED rather than left pending: an unreachable lot is exactly what the
            // syndic must see in the tracking table to convoke it another way instead.
            Convocation failed = convocation.recordDelivery(ConvocationDelivery.failed(ConvocationDeliveryId.newId(),
                    command.channel(), clock.instant(), command.requestedByUserId()));
            ConvocationView view = viewAssembler.toView(convocationRepository.save(failed), unit);
            throw ChannelCode.APP.equals(command.channel())
                    ? NoConvocationRecipientException.noAccount(view.unitNumber())
                    : NoConvocationRecipientException.noEmail(view.unitNumber());
        }

        Convocation sent = convocation.recordDelivery(ConvocationDelivery.sent(ConvocationDeliveryId.newId(),
                command.channel(), clock.instant(), null, command.requestedByUserId()));
        log.info("Convocation {} sent by {}", convocation.getId(), command.channel());
        return viewAssembler.toView(convocationRepository.save(sent), unit);
    }

    /**
     * Performs the send on ONE channel, and says whether it reached anybody.
     *
     * <p>This branch is the point of the whole change. The service used to mail
     * the owners and notify them in-app on every call, then record a single
     * delivery carrying whichever code the caller had asked for - so sending
     * "by APP" wrote APP while the email went out too, and the tracking table
     * asserted a route that was not the one taken. One channel now means one
     * act and one row, which is what lets the syndic press one button per
     * channel and read back what each one did.
     *
     * <p>Only EMAIL and APP ever get here: {@code requireSendable} has already
     * refused the manual channels and the automated ones no emitter exists for,
     * and {@code ConvocationChannelLookup.EMITTED_CODES} is the list this switch
     * must stay in step with. The final throw is that pact made visible - a code
     * added there and forgotten here fails loudly rather than sending nothing.
     */
    private boolean emit(ChannelCode channel, Convocation convocation, GeneralMeeting meeting, UnitInfo unit,
                          String propertyName, List<OwnerInfo> recipients, EntityId senderUserId) {
        if (ChannelCode.EMAIL.equals(channel)) {
            return emailTo(convocation, meeting, unit, propertyName, recipients);
        }
        if (ChannelCode.APP.equals(channel)) {
            return deliverInApp(meeting, unit, propertyName, recipients, senderUserId);
        }
        throw new IllegalStateException("No emitter for channel " + channel + " - ConvocationChannelLookup let "
                + "through a code SendConvocationService does not implement");
    }

    private boolean emailTo(Convocation convocation, GeneralMeeting meeting, UnitInfo unit, String propertyName,
                             List<OwnerInfo> recipients) {
        List<String> emails = recipients.stream().map(OwnerInfo::email)
                .filter(email -> email != null && !email.isBlank()).toList();
        if (emails.isEmpty()) {
            return false;
        }
        String subject = emailComposer.subject(propertyName, meeting);
        String body = emailComposer.htmlBody(propertyName, meeting, unit,
                linkComposer.link(convocation.getConfirmationToken()));
        for (String email : emails) {
            emailSenderPort.send(EmailVO.of(email), subject, body);
        }
        log.info("Convocation {} emailed to {} recipient(s)", convocation.getId(), emails.size());
        return true;
    }

    /**
     * The messagerie, plus the notification that points at it - one act, one
     * delivery row. Reaching a lot in the application means leaving its owners
     * something they can read and answer from, not only a bell.
     *
     * <p>Hence the change of stance from when this was a courtesy alongside the
     * email: it used to be swallowed because the email carried the record, and
     * here there is no email to fall back on. A failure has to come back as
     * FAILED so the syndic sees a lot to reach another way.
     *
     * <p>Still delegated to MeetingInAppDispatcher, and still for the original
     * reason: its REQUIRES_NEW suspends this transaction, so a failure marks its
     * own rather than this one. Without that, catching here would be decorative
     * - the messaging and notification services would join this transaction,
     * mark it rollback-only on their way out, and the commit would fail
     * afterwards whatever was caught, losing the FAILED row this method exists
     * to write.
     */
    private boolean deliverInApp(GeneralMeeting meeting, UnitInfo unit, String propertyName,
                                  List<OwnerInfo> recipients, EntityId senderUserId) {
        if (recipients.isEmpty()) {
            return false;
        }
        try {
            return dispatcher.deliverConvocation(recipients.stream().map(OwnerInfo::partyId).toList(), meeting,
                    senderUserId, emailComposer.subject(propertyName, meeting),
                    emailComposer.inAppBody(propertyName, meeting, unit),
                    ConvocationDocumentComposer.lotLabelOf(unit)) > 0;
        } catch (RuntimeException e) {
            log.warn("In-app delivery failed for general meeting {} on property {} - recorded as a failed delivery",
                    meeting.getId(), propertyName, e);
            return false;
        }
    }

}
