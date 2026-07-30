package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AccountingExerciseId(EntityId value) {

    public AccountingExerciseId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AccountingExerciseId newId() {
        return new AccountingExerciseId(EntityId.newId());
    }

    public static AccountingExerciseId of(UUID value) {
        return new AccountingExerciseId(EntityId.of(value));
    }

    public static AccountingExerciseId of(String value) {
        return new AccountingExerciseId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
