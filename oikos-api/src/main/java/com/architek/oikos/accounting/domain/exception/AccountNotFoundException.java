package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {

    public AccountNotFoundException(AccountId id) {
        super("Account not found with id: " + id);
    }

    public static AccountNotFoundException forHolder(EntityId holderId, AccountType accountType) {
        return new AccountNotFoundException("No " + accountType + " account found for holder id: " + holderId);
    }

    private AccountNotFoundException(String message) {
        super(message);
    }
}
