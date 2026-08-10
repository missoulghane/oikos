package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record JournalEntryLineView(JournalEntryLineId id, LedgerAccountId ledgerAccountId, EntityId auxiliaryUnitId,
                                    EntityId auxiliaryPartyId, EntryDirection direction, BigDecimal amount,
                                    String label) {

    public static JournalEntryLineView from(JournalEntryLine line) {
        return new JournalEntryLineView(line.getId(), line.getLedgerAccountId(), line.getAuxiliaryUnitId().orElse(null),
                line.getAuxiliaryPartyId().orElse(null), line.getDirection(), line.getAmount().value(), line.getLabel());
    }
}
