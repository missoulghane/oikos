package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.BalanceView;

public record BalanceResponse(String accountId, BigDecimal balance) {

    public static BalanceResponse from(BalanceView view) {
        return new BalanceResponse(view.accountId().toString(), view.balance());
    }
}
