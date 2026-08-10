package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** auxiliaryUnitId/auxiliaryPartyId are mutually exclusive - at most one is set (see JournalEntryLine). */
public record CreateJournalEntryLineCommand(LedgerAccountId ledgerAccountId, EntityId auxiliaryUnitId,
                                             EntityId auxiliaryPartyId, EntryDirection direction, BigDecimal amount,
                                             String label) {
}
