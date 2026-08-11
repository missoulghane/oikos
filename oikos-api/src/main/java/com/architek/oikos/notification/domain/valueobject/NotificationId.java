package com.architek.oikos.notification.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record NotificationId(EntityId value) {

    public NotificationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static NotificationId newId() {
        return new NotificationId(EntityId.newId());
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(EntityId.of(value));
    }

    public static NotificationId of(String value) {
        return new NotificationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
