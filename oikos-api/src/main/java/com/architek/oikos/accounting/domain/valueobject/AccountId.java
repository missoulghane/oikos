package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AccountId(EntityId value) {

    public AccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AccountId newId() {
        return new AccountId(EntityId.newId());
    }

    public static AccountId of(UUID value) {
        return new AccountId(EntityId.of(value));
    }

    public static AccountId of(String value) {
        return new AccountId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
