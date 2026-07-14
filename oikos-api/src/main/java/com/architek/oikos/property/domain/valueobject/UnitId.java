package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitId(EntityId value) {

    public UnitId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitId newId() {
        return new UnitId(EntityId.newId());
    }

    public static UnitId of(UUID value) {
        return new UnitId(EntityId.of(value));
    }

    public static UnitId of(String value) {
        return new UnitId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
