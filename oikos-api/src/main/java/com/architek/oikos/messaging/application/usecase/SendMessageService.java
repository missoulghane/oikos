package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.SendMessageCommand;
import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.port.in.SendMessageUseCase;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.ConversationNotFoundException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Access to the conversation itself is already enforced upstream by
 * {@code @PreAuthorize("@propertyAccess.canSendToConversation(...)")} - the
 * checks below are defense in depth (a bug in the SpEL guard must not let a
 * third party post into someone else's conversation, or a plain owner post
 * into a property's BROADCAST/BOARD_PRIVATE channel), not the primary
 * authorization boundary. A GROUP conversation requires the sender to be one
 * of its stored participants, and the client's claimed SenderIdentity is
 * honored only once verified (SenderIdentityValidator) - they really might
 * hold both hats on this property (case 2/4/6/7). BOARD_PRIVATE requires the
 * sender to currently hold a staff role (same population that can read it,
 * see PropertyAccessEvaluator); BROADCAST requires
 * Permission.MESSAGING_BROADCAST - the same population allowed to start the
 * channel (SendBroadcastMessageService). Both always post as BOARD - there is
 * no other identity a message in either could be sent under - regardless of
 * whatever the client sent.
 */
@Component
public class SendMessageService implements SendMessageUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberDisplayNameResolver memberDisplayNameResolver;
    private final UserAccessPort userAccessPort;
    private final SenderIdentityValidator senderIdentityValidator;
    private final Clock clock;

    public SendMessageService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                               MemberDisplayNameResolver memberDisplayNameResolver, UserAccessPort userAccessPort,
                               SenderIdentityValidator senderIdentityValidator, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
        this.userAccessPort = userAccessPort;
        this.senderIdentityValidator = senderIdentityValidator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MessageView send(SendMessageCommand command) {
        Conversation conversation = conversationRepository.findById(command.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException(command.conversationId()));

        SenderIdentity senderIdentity;
        if (conversation.getType() == ConversationType.GROUP) {
            if (!conversation.hasParticipant(command.senderId())) {
                throw new UnauthorizedException("sender is not a participant of this conversation");
            }
            senderIdentity = senderIdentityValidator.resolve(command.senderId(), conversation.getPropertyId(), command.senderIdentity());
        } else if (conversation.getType() == ConversationType.BOARD_PRIVATE) {
            if (!userAccessPort.managesProperty(command.senderId(), conversation.getPropertyId())) {
                throw new UnauthorizedException("sender is not allowed to post to this property's private board thread");
            }
            senderIdentity = SenderIdentity.BOARD;
        } else {
            if (!userAccessPort.canBroadcast(command.senderId(), conversation.getPropertyId())) {
                throw new UnauthorizedException("sender is not allowed to post to this property's broadcast channel");
            }
            senderIdentity = SenderIdentity.BOARD;
        }

        Message message = Message.post(MessageId.newId(), command.conversationId(), command.senderId(), senderIdentity,
                command.body(), clock.instant());
        Message saved = messageRepository.save(message);

        String senderName = memberDisplayNameResolver.namesByUserId(conversation.getPropertyId()).get(command.senderId());
        return new MessageView(saved.getId(), saved.getConversationId(), saved.getSenderId(), senderName,
                saved.getSenderIdentity(), saved.getBody().value(), saved.getCreatedDate(), true);
    }
}
