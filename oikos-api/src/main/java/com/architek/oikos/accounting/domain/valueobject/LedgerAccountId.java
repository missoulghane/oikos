package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record LedgerAccountId(EntityId value) {

    public LedgerAccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static LedgerAccountId newId() {
        return new LedgerAccountId(EntityId.newId());
    }

    public static LedgerAccountId of(UUID value) {
        return new LedgerAccountId(EntityId.of(value));
    }

    public static LedgerAccountId of(String value) {
        return new LedgerAccountId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
