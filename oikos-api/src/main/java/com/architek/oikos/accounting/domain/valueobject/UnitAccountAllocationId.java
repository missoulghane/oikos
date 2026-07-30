package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitAccountAllocationId(EntityId value) {

    public UnitAccountAllocationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitAccountAllocationId newId() {
        return new UnitAccountAllocationId(EntityId.newId());
    }

    public static UnitAccountAllocationId of(UUID value) {
        return new UnitAccountAllocationId(EntityId.of(value));
    }

    public static UnitAccountAllocationId of(String value) {
        return new UnitAccountAllocationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
