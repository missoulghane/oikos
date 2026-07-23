package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MovementId(EntityId value) {

    public MovementId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MovementId newId() {
        return new MovementId(EntityId.newId());
    }

    public static MovementId of(UUID value) {
        return new MovementId(EntityId.of(value));
    }

    public static MovementId of(String value) {
        return new MovementId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
