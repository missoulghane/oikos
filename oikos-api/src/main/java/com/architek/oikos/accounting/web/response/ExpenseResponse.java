package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.ExpenseView;

public record ExpenseResponse(String id, String propertyId, LocalDate date, String ledgerAccountId,
                               BigDecimal amount, String description, String receiptReference,
                               String journalEntryId) {

    public static ExpenseResponse from(ExpenseView view) {
        return new ExpenseResponse(view.id().toString(), view.propertyId().toString(), view.date(),
                view.ledgerAccountId().toString(), view.amount(), view.description(), view.receiptReference(),
                view.journalEntryId().toString());
    }
}
