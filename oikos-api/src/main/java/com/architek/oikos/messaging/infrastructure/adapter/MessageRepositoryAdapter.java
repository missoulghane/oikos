package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.repository.MessageRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.messaging.infrastructure.mapper.MessagePersistenceMapper;
import com.architek.oikos.messaging.infrastructure.persistence.MessageEntity;
import com.architek.oikos.messaging.infrastructure.persistence.MessageJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpaRepository;
    private final MessagePersistenceMapper mapper;

    public MessageRepositoryAdapter(MessageJpaRepository jpaRepository, MessagePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Message save(Message message) {
        MessageEntity saved = jpaRepository.save(mapper.toEntity(message));
        return mapper.toDomain(saved);
    }

    @Override
    public Page<Message> findRecentPage(ConversationId conversationId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<MessageEntity> springPage =
                jpaRepository.findByConversationIdOrderByCreatedDateDesc(conversationId.asUuid(), pageable);

        List<Message> ascending = new ArrayList<>(springPage.getContent().stream().map(mapper::toDomain).toList());
        Collections.reverse(ascending);

        return Page.of(ascending, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public Optional<Message> findLastMessage(ConversationId conversationId) {
        return jpaRepository.findFirstByConversationIdOrderByCreatedDateDesc(conversationId.asUuid()).map(mapper::toDomain);
    }

    @Override
    public long countByConversation(ConversationId conversationId) {
        return jpaRepository.countByConversationId(conversationId.asUuid());
    }

    @Override
    public long countByConversationAndSender(ConversationId conversationId, EntityId senderId) {
        return jpaRepository.countByConversationIdAndSenderId(conversationId.asUuid(), senderId.value());
    }

    @Override
    public long countUnread(ConversationId conversationId, MessageId lastReadMessageId) {
        if (lastReadMessageId == null) {
            return jpaRepository.countByConversationId(conversationId.asUuid());
        }
        return jpaRepository.countByConversationIdAndCreatedDateAfterMessage(conversationId.asUuid(), lastReadMessageId.asUuid());
    }
}
