package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.repository.ConversationRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.infrastructure.mapper.ConversationPersistenceMapper;
import com.architek.oikos.messaging.infrastructure.persistence.ConversationEntity;
import com.architek.oikos.messaging.infrastructure.persistence.ConversationJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpaRepository;
    private final ConversationPersistenceMapper mapper;

    public ConversationRepositoryAdapter(ConversationJpaRepository jpaRepository, ConversationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = jpaRepository.findById(conversation.getId().asUuid()).orElseGet(ConversationEntity::new);
        ConversationEntity saved = jpaRepository.save(mapper.toEntity(conversation, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findAllGroupByParticipant(EntityId userId) {
        return jpaRepository.findAllGroupByParticipant(userId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Conversation> findAllByPropertyIdsAndType(Collection<EntityId> propertyIds, ConversationType type) {
        if (propertyIds.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = propertyIds.stream().map(EntityId::value).toList();
        return jpaRepository.findByPropertyIdInAndType(ids, type).stream().map(mapper::toDomain).toList();
    }
}
