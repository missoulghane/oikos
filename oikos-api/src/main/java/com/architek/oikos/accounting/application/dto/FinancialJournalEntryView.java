package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;

public record FinancialJournalEntryView(FinancialJournalEntryId id, FinancialAccountId financialAccountId,
                                         LocalDate date, FinancialEntryType type, FinancialEntryDirection direction,
                                         BigDecimal amount, String label, String businessReference) {

    public static FinancialJournalEntryView from(FinancialJournalEntry entry) {
        return new FinancialJournalEntryView(entry.getId(), entry.getFinancialAccountId(), entry.getDate(),
                entry.getType(), entry.getDirection(), entry.getAmount().value(), entry.getLabel(),
                entry.getBusinessReference());
    }
}
