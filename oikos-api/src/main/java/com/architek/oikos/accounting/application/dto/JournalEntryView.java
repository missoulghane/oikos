package com.architek.oikos.accounting.application.dto;

import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record JournalEntryView(JournalEntryId id, EntityId propertyId, AccountingExerciseId exerciseId,
                                PeriodId periodId, JournalCode journalCode, LedgerAccountId treasuryAccountId,
                                LocalDate pieceDate, Integer pieceNumber, String externalReference,
                                JournalEntryStatus status, JournalEntryId originalEntryId, EntityId createdByUserId,
                                List<JournalEntryLineView> lines) {

    public static JournalEntryView from(JournalEntry entry) {
        return new JournalEntryView(entry.getId(), entry.getPropertyId(), entry.getExerciseId(), entry.getPeriodId(),
                entry.getJournalCode(), entry.getTreasuryAccountId().orElse(null), entry.getPieceDate(),
                entry.getPieceNumber().orElse(null), entry.getExternalReference().orElse(null), entry.getStatus(),
                entry.getOriginalEntryId().orElse(null), entry.getCreatedByUserId(),
                entry.getLines().stream().map(JournalEntryLineView::from).toList());
    }
}
