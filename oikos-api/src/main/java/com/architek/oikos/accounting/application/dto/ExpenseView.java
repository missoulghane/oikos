package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ExpenseView(ExpenseId id, EntityId propertyId, LocalDate date, LedgerAccountId ledgerAccountId,
                           BigDecimal amount, String description, String receiptReference,
                           JournalEntryId journalEntryId) {

    public static ExpenseView from(Expense expense) {
        return new ExpenseView(expense.getId(), expense.getPropertyId(), expense.getDate(),
                expense.getLedgerAccountId(), expense.getAmount().value(), expense.getDescription().orElse(null),
                expense.getReceiptReference().orElse(null), expense.getJournalEntryId());
    }
}
