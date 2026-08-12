package com.architek.oikos.notification.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record DevicePushTokenId(EntityId value) {

    public DevicePushTokenId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static DevicePushTokenId newId() {
        return new DevicePushTokenId(EntityId.newId());
    }

    public static DevicePushTokenId of(UUID value) {
        return new DevicePushTokenId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
