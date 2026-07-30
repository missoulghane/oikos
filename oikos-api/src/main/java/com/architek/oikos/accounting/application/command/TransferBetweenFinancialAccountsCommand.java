package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record TransferBetweenFinancialAccountsCommand(EntityId propertyId, FinancialAccountId fromAccountId,
                                                        FinancialAccountId toAccountId, BigDecimal amount,
                                                        LocalDate date, String label, EntityId createdByUserId) {
}
