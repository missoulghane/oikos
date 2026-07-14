package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitOwnershipId(EntityId value) {

    public UnitOwnershipId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitOwnershipId newId() {
        return new UnitOwnershipId(EntityId.newId());
    }

    public static UnitOwnershipId of(UUID value) {
        return new UnitOwnershipId(EntityId.of(value));
    }

    public static UnitOwnershipId of(String value) {
        return new UnitOwnershipId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
