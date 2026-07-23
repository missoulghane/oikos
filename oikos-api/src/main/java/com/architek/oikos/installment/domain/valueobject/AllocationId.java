package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AllocationId(EntityId value) {

    public AllocationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AllocationId newId() {
        return new AllocationId(EntityId.newId());
    }

    public static AllocationId of(UUID value) {
        return new AllocationId(EntityId.of(value));
    }

    public static AllocationId of(String value) {
        return new AllocationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
