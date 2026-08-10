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
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Access to the conversation itself is already enforced upstream by
 * {@code @PreAuthorize("@propertyAccess.canSendToConversation(...)")} - the
 * checks below are defense in depth (a bug in the SpEL guard must not let a
 * third party post into someone else's conversation, or a plain owner post
 * into a property's BROADCAST channel), not the primary authorization
 * boundary. A GROUP conversation requires the sender to be one of its stored
 * participants; a BROADCAST conversation has none to check against, so it
 * requires the sender to currently hold Permission.MESSAGING_BROADCAST on
 * the conversation's property instead (UserAccessPort.canBroadcast) - the
 * same population allowed to start the channel in the first place
 * (SendBroadcastMessageService), narrower than the "any member may read it"
 * population isConversationParticipant gates separately.
 */
@Component
public class SendMessageService implements SendMessageUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberDisplayNameResolver memberDisplayNameResolver;
    private final UserAccessPort userAccessPort;
    private final Clock clock;

    public SendMessageService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                               MemberDisplayNameResolver memberDisplayNameResolver, UserAccessPort userAccessPort, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
        this.userAccessPort = userAccessPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MessageView send(SendMessageCommand command) {
        Conversation conversation = conversationRepository.findById(command.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException(command.conversationId()));

        if (conversation.getType() == ConversationType.GROUP) {
            if (!conversation.hasParticipant(command.senderId())) {
                throw new UnauthorizedException("sender is not a participant of this conversation");
            }
        } else if (!userAccessPort.canBroadcast(command.senderId(), conversation.getPropertyId())) {
            throw new UnauthorizedException("sender is not allowed to post to this property's broadcast channel");
        }

        Message message = Message.post(MessageId.newId(), command.conversationId(), command.senderId(), command.body(),
                clock.instant());
        Message saved = messageRepository.save(message);

        String senderName = memberDisplayNameResolver.namesByUserId(conversation.getPropertyId()).get(command.senderId());
        return new MessageView(saved.getId(), saved.getConversationId(), saved.getSenderId(), senderName,
                saved.getBody().value(), saved.getCreatedDate(), true);
    }
}
