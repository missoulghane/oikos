package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record FinancialJournalEntryId(EntityId value) {

    public FinancialJournalEntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static FinancialJournalEntryId newId() {
        return new FinancialJournalEntryId(EntityId.newId());
    }

    public static FinancialJournalEntryId of(UUID value) {
        return new FinancialJournalEntryId(EntityId.of(value));
    }

    public static FinancialJournalEntryId of(String value) {
        return new FinancialJournalEntryId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
