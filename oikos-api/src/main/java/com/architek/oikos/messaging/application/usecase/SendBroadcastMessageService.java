package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;

/**
 * Find-or-create the property's single persistent BROADCAST channel (unique
 * index on property_id where type = 'BROADCAST', see V17__messaging.sql),
 * then posts the message - never a one-off message detached from a
 * conversation, consistent with "reprendre une conversation existante".
 * Authorization (canBroadcastOnProperty, i.e. Permission.MESSAGING_BROADCAST)
 * is enforced upstream by @PreAuthorize; this service trusts its caller.
 */
@Component
public class SendBroadcastMessageService implements SendBroadcastMessageUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final Clock clock;

    public SendBroadcastMessageService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                                        Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConversationId send(SendBroadcastMessageCommand command) {
        Conversation conversation = conversationRepository.findBroadcastConversation(command.propertyId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.createBroadcast(ConversationId.newId(), command.propertyId(), command.senderId())));

        Message message = Message.post(MessageId.newId(), conversation.getId(), command.senderId(), command.body(),
                clock.instant());
        messageRepository.save(message);

        return conversation.getId();
    }
}
