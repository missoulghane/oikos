package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * A holder (a unit or a property) must have exactly one account of its type.
 */
public class DuplicateAccountException extends BusinessException {

    public DuplicateAccountException(EntityId holderId, AccountType accountType) {
        super("A " + accountType + " account already exists for holder id: " + holderId);
    }
}
