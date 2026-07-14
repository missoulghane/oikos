package com.architek.oikos.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UserId(EntityId value) {

    public UserId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UserId newId() {
        return new UserId(EntityId.newId());
    }

    public static UserId of(UUID value) {
        return new UserId(EntityId.of(value));
    }

    public static UserId of(String value) {
        return new UserId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
