package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record JournalEntryId(EntityId value) {

    public JournalEntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static JournalEntryId newId() {
        return new JournalEntryId(EntityId.newId());
    }

    public static JournalEntryId of(UUID value) {
        return new JournalEntryId(EntityId.of(value));
    }

    public static JournalEntryId of(String value) {
        return new JournalEntryId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
