package com.architek.oikos.accounting.application.command;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateFinancialAccountCommand(EntityId propertyId, String name, FinancialAccountType type,
                                             String currency) {
}
