package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record BuildingId(EntityId value) {

    public BuildingId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static BuildingId newId() {
        return new BuildingId(EntityId.newId());
    }

    public static BuildingId of(UUID value) {
        return new BuildingId(EntityId.of(value));
    }

    public static BuildingId of(String value) {
        return new BuildingId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
