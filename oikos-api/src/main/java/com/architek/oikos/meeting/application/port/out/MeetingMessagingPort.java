package com.architek.oikos.meeting.application.port.out;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Posts a lot's convocation into the application's messagerie. Kept behind a
 * port so the messaging module's command and value-object vocabulary never
 * reaches this module's use cases (rule 4/6), same patron as
 * {@link MeetingNotificationPort}.
 *
 * <p>Note what this port does NOT carry: an attachment. The messagerie has no
 * such thing, and the convocation that has legal standing is the PDF. What goes
 * out here is the covering note - the same wording the email carries - and the
 * document stays where a copropriétaire can already fetch it, from their own
 * space. A port pretending otherwise would promise a delivery the module
 * underneath cannot make.
 */
public interface MeetingMessagingPort {

    /**
     * @throws RuntimeException when the message cannot be posted - the caller
     *         records the attempt as a failed delivery rather than letting it
     *         pass silently.
     */
    void postConvocation(ConvocationMessage message);

    /**
     * senderUserId is the syndic performing the send: the messagerie has no
     * system sender, and attributing a convocation to nobody would leave the
     * copropriétaire a thread they cannot reply to.
     *
     * <p>lotLabel travels separately from the body because it is structural:
     * it fills the conversation's "concerne" hint, which is what tells an owner
     * of three lots which of them a thread is about.
     */
    record ConvocationMessage(EntityId propertyId, EntityId senderUserId, Set<EntityId> recipientUserIds,
                               String subject, String body, String lotLabel) {
    }
}
