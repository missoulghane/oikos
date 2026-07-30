package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitAccountId(EntityId value) {

    public UnitAccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitAccountId newId() {
        return new UnitAccountId(EntityId.newId());
    }

    public static UnitAccountId of(UUID value) {
        return new UnitAccountId(EntityId.of(value));
    }

    public static UnitAccountId of(String value) {
        return new UnitAccountId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
