package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PeriodId(EntityId value) {

    public PeriodId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PeriodId newId() {
        return new PeriodId(EntityId.newId());
    }

    public static PeriodId of(UUID value) {
        return new PeriodId(EntityId.of(value));
    }

    public static PeriodId of(String value) {
        return new PeriodId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
