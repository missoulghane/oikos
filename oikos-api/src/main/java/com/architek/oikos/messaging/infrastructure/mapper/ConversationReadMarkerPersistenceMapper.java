package com.architek.oikos.messaging.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.messaging.infrastructure.persistence.ConversationReadMarkerEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface ConversationReadMarkerPersistenceMapper {

    default ConversationReadMarkerEntity toEntity(ConversationReadMarker marker) {
        ConversationReadMarkerEntity entity = new ConversationReadMarkerEntity();
        entity.setConversationId(marker.getConversationId().asUuid());
        entity.setUserId(marker.getUserId().value());
        entity.setLastReadMessageId(marker.getLastReadMessageId() != null ? marker.getLastReadMessageId().asUuid() : null);
        entity.setLastReadAt(marker.getLastReadAt());
        return entity;
    }

    default ConversationReadMarker toDomain(ConversationReadMarkerEntity entity) {
        return ConversationReadMarker.reconstruct(ConversationId.of(entity.getConversationId()), EntityId.of(entity.getUserId()),
                entity.getLastReadMessageId() != null ? MessageId.of(entity.getLastReadMessageId()) : null, entity.getLastReadAt());
    }
}
