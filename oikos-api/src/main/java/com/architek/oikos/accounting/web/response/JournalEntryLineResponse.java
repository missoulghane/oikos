package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.JournalEntryLineView;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;

public record JournalEntryLineResponse(String id, String ledgerAccountId, String auxiliaryUnitId,
                                        String auxiliaryPartyId, EntryDirection direction, BigDecimal amount,
                                        String label) {

    public static JournalEntryLineResponse from(JournalEntryLineView view) {
        return new JournalEntryLineResponse(view.id().toString(), view.ledgerAccountId().toString(),
                view.auxiliaryUnitId() == null ? null : view.auxiliaryUnitId().toString(),
                view.auxiliaryPartyId() == null ? null : view.auxiliaryPartyId().toString(), view.direction(),
                view.amount(), view.label());
    }
}
