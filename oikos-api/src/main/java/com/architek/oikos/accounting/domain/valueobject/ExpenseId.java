package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ExpenseId(EntityId value) {

    public ExpenseId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ExpenseId newId() {
        return new ExpenseId(EntityId.newId());
    }

    public static ExpenseId of(UUID value) {
        return new ExpenseId(EntityId.of(value));
    }

    public static ExpenseId of(String value) {
        return new ExpenseId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
