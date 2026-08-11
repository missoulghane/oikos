package com.architek.oikos.messaging.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.StartBoardConversationCommand;
import com.architek.oikos.messaging.application.port.in.StartBoardConversationUseCase;
import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.exception.RecipientNotPropertyMemberException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;

/**
 * Starts a new BOARD_PRIVATE thread - always a brand-new one, never a
 * find-or-create (same "New message" semantics as GROUP, see Conversation's
 * javadoc): a property can have several concurrent private board threads,
 * one per topic. Unlike StartGroupConversationService there is no recipient
 * list to validate - membership is resolved dynamically from the property's
 * current staff roster at read time (see PropertyAccessEvaluator), the same
 * mechanism BROADCAST uses, so only the sender's own eligibility is checked
 * here. senderIdentity is always BOARD - there is no other identity a
 * private board thread could be posted under.
 */
@Component
public class StartBoardConversationService implements StartBoardConversationUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserAccessPort userAccessPort;
    private final Clock clock;

    public StartBoardConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                                          UserAccessPort userAccessPort, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userAccessPort = userAccessPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConversationId start(StartBoardConversationCommand command) {
        if (!userAccessPort.managesProperty(command.senderId(), command.propertyId())) {
            throw new RecipientNotPropertyMemberException(
                    "sender is not a staff member of property " + command.propertyId());
        }

        Conversation created = Conversation.createBoardPrivate(ConversationId.newId(), command.propertyId(),
                command.senderId(), command.subject());
        ConversationId conversationId = conversationRepository.save(created).getId();

        Message firstMessage = Message.post(MessageId.newId(), conversationId, command.senderId(), SenderIdentity.BOARD,
                command.body(), clock.instant());
        messageRepository.save(firstMessage);

        return conversationId;
    }
}
