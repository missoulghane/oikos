package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitTypeDefinitionId(EntityId value) {

    public UnitTypeDefinitionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitTypeDefinitionId newId() {
        return new UnitTypeDefinitionId(EntityId.newId());
    }

    public static UnitTypeDefinitionId of(UUID value) {
        return new UnitTypeDefinitionId(EntityId.of(value));
    }

    public static UnitTypeDefinitionId of(String value) {
        return new UnitTypeDefinitionId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
