package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountStatus;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;

public record FinancialAccountView(FinancialAccountId id, String name, FinancialAccountType type, String currency,
                                    BigDecimal balance, FinancialAccountStatus status) {

    public static FinancialAccountView from(FinancialAccount account) {
        return new FinancialAccountView(account.getId(), account.getName(), account.getType(), account.getCurrency(),
                account.getBalance(), account.getStatus());
    }
}
