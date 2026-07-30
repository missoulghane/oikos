package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.FinancialJournalEntryView;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;

public record FinancialJournalEntryResponse(String id, String financialAccountId, LocalDate date,
                                             FinancialEntryType type, FinancialEntryDirection direction,
                                             BigDecimal amount, String label, String businessReference) {

    public static FinancialJournalEntryResponse from(FinancialJournalEntryView view) {
        return new FinancialJournalEntryResponse(view.id().toString(), view.financialAccountId().toString(),
                view.date(), view.type(), view.direction(), view.amount(), view.label(), view.businessReference());
    }
}
