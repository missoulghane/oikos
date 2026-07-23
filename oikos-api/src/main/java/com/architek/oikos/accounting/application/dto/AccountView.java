package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AccountView(AccountId id, EntityId holderId, AccountType accountType, BigDecimal balance) {

    public static AccountView from(Account account) {
        return new AccountView(account.getId(), account.getHolderId(), account.getAccountType(), account.getBalance());
    }
}
