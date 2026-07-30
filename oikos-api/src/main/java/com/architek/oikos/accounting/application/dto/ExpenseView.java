package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;

public record ExpenseView(ExpenseId id, FinancialAccountId financialAccountId, LocalDate date, String category,
                           String provider, BigDecimal amount, String description, String receiptReference) {

    public static ExpenseView from(Expense expense) {
        return new ExpenseView(expense.getId(), expense.getFinancialAccountId(), expense.getDate(),
                expense.getCategory(), expense.getProvider(), expense.getAmount().value(), expense.getDescription(),
                expense.getReceiptReference());
    }
}
