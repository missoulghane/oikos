package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitTypePricingId(EntityId value) {

    public UnitTypePricingId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static UnitTypePricingId newId() {
        return new UnitTypePricingId(EntityId.newId());
    }

    public static UnitTypePricingId of(UUID value) {
        return new UnitTypePricingId(EntityId.of(value));
    }

    public static UnitTypePricingId of(String value) {
        return new UnitTypePricingId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
