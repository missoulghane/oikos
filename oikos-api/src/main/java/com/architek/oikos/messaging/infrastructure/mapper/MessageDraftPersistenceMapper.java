package com.architek.oikos.messaging.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.architek.oikos.messaging.domain.model.MessageDraft;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.messaging.infrastructure.persistence.MessageDraftEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface MessageDraftPersistenceMapper {

    default MessageDraftEntity toEntity(MessageDraft draft) {
        return toEntity(draft, new MessageDraftEntity());
    }

    default MessageDraftEntity toEntity(MessageDraft draft, MessageDraftEntity entity) {
        entity.setId(draft.getId().asUuid());
        entity.setPropertyId(draft.getPropertyId().value());
        entity.setCreatedBy(draft.getCreatedBy().value());
        entity.setBroadcast(draft.isBroadcast());
        entity.setSubject(draft.getSubject());
        entity.setBody(draft.getBody());
        entity.setRecipientUserIds(draft.getRecipientUserIds().stream().map(EntityId::value).collect(Collectors.toSet()));
        return entity;
    }

    default MessageDraft toDomain(MessageDraftEntity entity) {
        Set<EntityId> recipientUserIds = entity.getRecipientUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
        return MessageDraft.reconstruct(MessageDraftId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                EntityId.of(entity.getCreatedBy()), recipientUserIds, entity.isBroadcast(), entity.getSubject(),
                entity.getBody(), entity.getCreatedDate(), entity.getLastModifiedDate());
    }
}
