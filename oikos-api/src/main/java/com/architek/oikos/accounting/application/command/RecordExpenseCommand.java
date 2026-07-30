package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecordExpenseCommand(EntityId propertyId, FinancialAccountId financialAccountId, LocalDate date,
                                    String category, String provider, BigDecimal amount, String description,
                                    String receiptReference, EntityId createdByUserId) {
}
