package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentCallId(EntityId value) {

    public InstallmentCallId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InstallmentCallId newId() {
        return new InstallmentCallId(EntityId.newId());
    }

    public static InstallmentCallId of(UUID value) {
        return new InstallmentCallId(EntityId.of(value));
    }

    public static InstallmentCallId of(String value) {
        return new InstallmentCallId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
