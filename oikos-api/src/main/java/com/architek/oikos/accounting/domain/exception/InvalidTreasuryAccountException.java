package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a caller-chosen treasury account (Partie 3: a property may now
 * have several BANK accounts, so callers name which one is impacted) does
 * not exist, does not belong to the property, or does not carry an allowed
 * role (BANK/CASH, or BANK only for bank charges) - see TreasuryAccountResolver.
 */
public class InvalidTreasuryAccountException extends ResourceNotFoundException {

    public InvalidTreasuryAccountException(LedgerAccountId accountId, EntityId propertyId) {
        super("Ledger account " + accountId + " is not a valid treasury account for property " + propertyId);
    }
}
