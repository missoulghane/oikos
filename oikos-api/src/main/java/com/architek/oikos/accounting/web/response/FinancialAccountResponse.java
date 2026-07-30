package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.FinancialAccountView;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountStatus;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;

public record FinancialAccountResponse(String id, String name, FinancialAccountType type, String currency,
                                        BigDecimal balance, FinancialAccountStatus status) {

    public static FinancialAccountResponse from(FinancialAccountView view) {
        return new FinancialAccountResponse(view.id().toString(), view.name(), view.type(), view.currency(),
                view.balance(), view.status());
    }
}
