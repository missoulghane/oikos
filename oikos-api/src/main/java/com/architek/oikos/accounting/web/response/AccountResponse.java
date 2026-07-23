package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.domain.valueobject.AccountType;

public record AccountResponse(String id, String holderId, AccountType accountType, BigDecimal balance) {

    public static AccountResponse from(AccountView view) {
        return new AccountResponse(view.id().toString(), view.holderId().toString(), view.accountType(), view.balance());
    }
}
