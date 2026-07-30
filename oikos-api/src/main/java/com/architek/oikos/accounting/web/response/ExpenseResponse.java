package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.ExpenseView;

public record ExpenseResponse(String id, String financialAccountId, LocalDate date, String category, String provider,
                               BigDecimal amount, String description, String receiptReference) {

    public static ExpenseResponse from(ExpenseView view) {
        return new ExpenseResponse(view.id().toString(), view.financialAccountId().toString(), view.date(),
                view.category(), view.provider(), view.amount(), view.description(), view.receiptReference());
    }
}
