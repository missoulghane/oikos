package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record FinancialAccountId(EntityId value) {

    public FinancialAccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static FinancialAccountId newId() {
        return new FinancialAccountId(EntityId.newId());
    }

    public static FinancialAccountId of(UUID value) {
        return new FinancialAccountId(EntityId.of(value));
    }

    public static FinancialAccountId of(String value) {
        return new FinancialAccountId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
