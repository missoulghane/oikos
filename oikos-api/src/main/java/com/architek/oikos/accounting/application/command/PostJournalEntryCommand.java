package com.architek.oikos.accounting.application.command;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

public record PostJournalEntryCommand(JournalEntryId entryId) {
}
