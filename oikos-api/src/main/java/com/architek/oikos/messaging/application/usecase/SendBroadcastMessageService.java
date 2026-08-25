package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;

/**
 * Un envoi à toute la copropriété crée sa propre conversation, à chaque fois -
 * jamais une reprise du canal existant, comme c'était le cas jusqu'ici
 * (find-or-create sur l'unique BROADCAST de la copropriété). Un envoi groupé
 * n'est pas un fil de discussion : deux annonces sans rapport se retrouvaient
 * bout à bout sous un même titre, et l'objet de chacune n'existait nulle part.
 * Même sémantique « nouveau message » que GROUP et BOARD_PRIVATE désormais.
 *
 * <p>Authorization (canBroadcastOnProperty, i.e. Permission.MESSAGING_BROADCAST)
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
        Conversation conversation = conversationRepository.save(Conversation.createBroadcast(
                ConversationId.newId(), command.propertyId(), command.senderId(), command.subject()));

        Message message = Message.post(MessageId.newId(), conversation.getId(), command.senderId(), SenderIdentity.BOARD,
                command.body(), clock.instant());
        messageRepository.save(message);

        return conversation.getId();
    }
}
