package com.architek.oikos.accounting.application.command;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateJournalEntryDraftCommand(EntityId propertyId, JournalCode journalCode,
                                              LedgerAccountId treasuryAccountId, LocalDate pieceDate,
                                              String externalReference, EntityId createdByUserId,
                                              List<CreateJournalEntryLineCommand> lines) {
}
