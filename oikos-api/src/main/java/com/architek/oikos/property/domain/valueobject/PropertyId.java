package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PropertyId(EntityId value) {

    public PropertyId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PropertyId newId() {
        return new PropertyId(EntityId.newId());
    }

    public static PropertyId of(UUID value) {
        return new PropertyId(EntityId.of(value));
    }

    public static PropertyId of(String value) {
        return new PropertyId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
