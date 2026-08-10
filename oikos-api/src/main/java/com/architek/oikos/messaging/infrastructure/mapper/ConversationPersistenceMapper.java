package com.architek.oikos.messaging.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.infrastructure.persistence.ConversationEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface ConversationPersistenceMapper {

    default ConversationEntity toEntity(Conversation conversation) {
        return toEntity(conversation, new ConversationEntity());
    }

    default ConversationEntity toEntity(Conversation conversation, ConversationEntity entity) {
        entity.setId(conversation.getId().asUuid());
        entity.setPropertyId(conversation.getPropertyId().value());
        entity.setType(conversation.getType());
        entity.setCreatedBy(conversation.getCreatedBy().value());
        entity.setSubject(conversation.getSubject() != null ? conversation.getSubject().value() : null);
        entity.setParticipantUserIds(conversation.getParticipantUserIds().stream().map(EntityId::value).collect(Collectors.toSet()));
        return entity;
    }

    default Conversation toDomain(ConversationEntity entity) {
        Set<EntityId> participantUserIds = entity.getParticipantUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
        ConversationSubject subject = entity.getSubject() != null ? ConversationSubject.of(entity.getSubject()) : null;
        return Conversation.reconstruct(ConversationId.of(entity.getId()), EntityId.of(entity.getPropertyId()), entity.getType(),
                EntityId.of(entity.getCreatedBy()), participantUserIds, subject, entity.getCreatedDate());
    }
}
