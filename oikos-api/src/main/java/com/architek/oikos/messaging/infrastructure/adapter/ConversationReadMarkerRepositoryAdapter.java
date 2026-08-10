package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.infrastructure.mapper.ConversationReadMarkerPersistenceMapper;
import com.architek.oikos.messaging.infrastructure.persistence.ConversationReadMarkerJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ConversationReadMarkerRepositoryAdapter implements ConversationReadMarkerRepository {

    private final ConversationReadMarkerJpaRepository jpaRepository;
    private final ConversationReadMarkerPersistenceMapper mapper;

    public ConversationReadMarkerRepositoryAdapter(ConversationReadMarkerJpaRepository jpaRepository,
                                                     ConversationReadMarkerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ConversationReadMarker save(ConversationReadMarker marker) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(marker)));
    }

    @Override
    public Optional<ConversationReadMarker> findByConversationIdAndUserId(ConversationId conversationId, EntityId userId) {
        return jpaRepository.findByConversationIdAndUserId(conversationId.asUuid(), userId.value()).map(mapper::toDomain);
    }
}
