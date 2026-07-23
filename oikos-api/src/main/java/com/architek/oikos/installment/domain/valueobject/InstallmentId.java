package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentId(EntityId value) {

    public InstallmentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InstallmentId newId() {
        return new InstallmentId(EntityId.newId());
    }

    public static InstallmentId of(UUID value) {
        return new InstallmentId(EntityId.of(value));
    }

    public static InstallmentId of(String value) {
        return new InstallmentId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
