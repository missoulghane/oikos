package com.architek.oikos.messaging.application.usecase;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.port.in.ListConversationMessagesUseCase;
import com.architek.oikos.messaging.application.query.ListConversationMessagesQuery;
import com.architek.oikos.messaging.domain.exception.ConversationNotFoundException;
import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ListConversationMessagesService implements ListConversationMessagesUseCase {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberDisplayNameResolver memberDisplayNameResolver;

    public ListConversationMessagesService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                                            MemberDisplayNameResolver memberDisplayNameResolver) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.memberDisplayNameResolver = memberDisplayNameResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageView> listMessages(ListConversationMessagesQuery query) {
        Conversation conversation = conversationRepository.findById(query.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException(query.conversationId()));

        Page<Message> messages = messageRepository.findRecentPage(query.conversationId(), query.pageRequest());
        Map<EntityId, String> namesByUserId = memberDisplayNameResolver.namesByUserId(conversation.getPropertyId());

        return messages.map(message -> new MessageView(message.getId(), message.getConversationId(), message.getSenderId(),
                namesByUserId.get(message.getSenderId()), message.getBody().value(), message.getCreatedDate(),
                message.getSenderId().equals(query.userId())));
    }
}
