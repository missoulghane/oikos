package com.architek.oikos.accounting.domain.valueobject;

import java.time.LocalDate;

/**
 * Search criteria for the treasury journal listing (spec &sect;16 "Journal":
 * filtres exercice / periode / type d'operation / compte). Any field left
 * null matches every value for that criterion.
 */
public record FinancialJournalEntryFilter(AccountingExerciseId exerciseId, FinancialAccountId financialAccountId,
                                           FinancialEntryType type, LocalDate dateFrom, LocalDate dateTo) {

    public FinancialJournalEntryFilter {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must not be after dateTo");
        }
    }

    public static FinancialJournalEntryFilter defaultFilter() {
        return new FinancialJournalEntryFilter(null, null, null, null, null);
    }
}
