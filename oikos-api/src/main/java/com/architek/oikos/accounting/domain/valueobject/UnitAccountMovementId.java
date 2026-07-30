package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitAccountMovementId(EntityId value) {

    public UnitAccountMovementId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitAccountMovementId newId() {
        return new UnitAccountMovementId(EntityId.newId());
    }

    public static UnitAccountMovementId of(UUID value) {
        return new UnitAccountMovementId(EntityId.of(value));
    }

    public static UnitAccountMovementId of(String value) {
        return new UnitAccountMovementId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
