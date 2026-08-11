package com.architek.oikos.notification.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.notification.domain.model.Notification;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.notification.infrastructure.persistence.NotificationEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface NotificationPersistenceMapper {

    default NotificationEntity toEntity(Notification notification) {
        return toEntity(notification, new NotificationEntity());
    }

    default NotificationEntity toEntity(Notification notification, NotificationEntity entity) {
        entity.setId(notification.getId().asUuid());
        entity.setUserId(notification.getRecipientUserId().value());
        entity.setPropertyId(notification.getPropertyId() != null ? notification.getPropertyId().value() : null);
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setBody(notification.getBody());
        entity.setLinkPath(notification.getLinkPath());
        entity.setReadAt(notification.getReadAt());
        return entity;
    }

    default Notification toDomain(NotificationEntity entity) {
        EntityId propertyId = entity.getPropertyId() != null ? EntityId.of(entity.getPropertyId()) : null;
        return Notification.reconstruct(NotificationId.of(entity.getId()), EntityId.of(entity.getUserId()), propertyId,
                entity.getType(), entity.getTitle(), entity.getBody(), entity.getLinkPath(), entity.getReadAt(),
                entity.getCreatedDate());
    }
}
