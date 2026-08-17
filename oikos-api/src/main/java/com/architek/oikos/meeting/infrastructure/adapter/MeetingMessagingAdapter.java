package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.MeetingMessagingPort;
import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Drops a convocation into the messagerie as a GROUP conversation, one per
 * convocation.
 *
 * <p><b>GROUP and not BROADCAST</b>, although a convocation goes to the whole
 * copropriété. The broadcast channel is unique per property and addresses
 * everyone at once: it would produce one message for a hundred lots and no
 * per-lot trace, while the entire module is built on one convocation per lot.
 * The recipients here are one lot's owners, and {@code concernsUnit} names the
 * lot - which is what tells an owner of three lots which thread is which.
 *
 * <p><b>A new thread every time</b>, because StartGroupConversationService never
 * does a find-or-create (deliberate "New message" semantics, see Conversation).
 * That suits this caller: a re-send and a reminder are separate attempts, each
 * with its own delivery row, and a thread apiece mirrors them exactly.
 *
 * <p><b>BOARD, stated rather than left to be inferred.</b> The validator asks
 * the client to choose only when the sender holds both hats on the property,
 * and a syndic who happens to own a lot there would otherwise be refused
 * mid-run. A convocation is sent in the manager's capacity, never the owner's,
 * so there is nothing genuinely ambiguous to resolve.
 */
@Component
public class MeetingMessagingAdapter implements MeetingMessagingPort {

    private final StartGroupConversationUseCase startGroupConversationUseCase;

    public MeetingMessagingAdapter(StartGroupConversationUseCase startGroupConversationUseCase) {
        this.startGroupConversationUseCase = startGroupConversationUseCase;
    }

    @Override
    public void postConvocation(ConvocationMessage message) {
        // A sender cannot be their own recipient, and a syndic who owns a lot in the
        // copropriété would otherwise appear in both lists and have the whole run refused.
        Set<EntityId> recipients = message.recipientUserIds().stream()
                .filter(userId -> !userId.equals(message.senderUserId())).collect(Collectors.toSet());
        if (recipients.isEmpty()) {
            return;
        }
        startGroupConversationUseCase.start(new StartGroupConversationCommand(message.propertyId(),
                message.senderUserId(), recipients, ConversationSubject.of(truncate(message.subject(), 200)),
                MessageBody.of(truncate(message.body(), 4000)), SenderIdentity.BOARD, message.lotLabel()));
    }

    /**
     * Both value objects refuse anything longer and would throw, which on this
     * path means a convocation recorded as failed for a meeting whose title ran
     * long. Truncating is the lesser evil: the body is a covering note, the
     * convocation itself is the PDF, and a message cut short still reaches its
     * recipient.
     */
    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
