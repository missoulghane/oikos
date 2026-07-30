package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecordExceptionalDepositCommand(EntityId propertyId, FinancialAccountId financialAccountId,
                                               BigDecimal amount, LocalDate date, String label,
                                               EntityId createdByUserId) {
}
