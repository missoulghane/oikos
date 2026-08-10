package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PaymentId(EntityId value) {

    public PaymentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PaymentId newId() {
        return new PaymentId(EntityId.newId());
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(EntityId.of(value));
    }

    public static PaymentId of(String value) {
        return new PaymentId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
