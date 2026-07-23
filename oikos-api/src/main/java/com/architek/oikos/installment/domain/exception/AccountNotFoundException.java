package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when the ledger (accounting) reports no account for a holder that
 * installment needs one for - installment does not depend on accounting's own
 * AccountNotFoundException (rule 4).
 */
public class AccountNotFoundException extends ResourceNotFoundException {

    private AccountNotFoundException(String message) {
        super(message);
    }

    public static AccountNotFoundException forProperty(EntityId propertyId) {
        return new AccountNotFoundException("No PROPERTY account found for holder id: " + propertyId);
    }

    public static AccountNotFoundException forUnit(EntityId unitId) {
        return new AccountNotFoundException("No UNIT account found for holder id: " + unitId);
    }
}
