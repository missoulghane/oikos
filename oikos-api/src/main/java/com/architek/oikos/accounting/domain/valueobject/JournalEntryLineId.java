package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record JournalEntryLineId(EntityId value) {

    public JournalEntryLineId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static JournalEntryLineId newId() {
        return new JournalEntryLineId(EntityId.newId());
    }

    public static JournalEntryLineId of(UUID value) {
        return new JournalEntryLineId(EntityId.of(value));
    }

    public static JournalEntryLineId of(String value) {
        return new JournalEntryLineId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
