package com.architek.oikos.accounting.application.query;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

public record GetJournalEntryQuery(JournalEntryId entryId) {
}
