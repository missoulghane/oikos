package com.architek.oikos.accounting.application.command;

import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * reversalDate is used only when the original entry's exercise is already
 * closed - otherwise the mirror is dated like the original (see
 * ReverseJournalEntryService).
 */
public record ReverseJournalEntryCommand(EntityId propertyId, JournalEntryId entryId, LocalDate reversalDate,
                                          EntityId reversedByUserId) {
}
