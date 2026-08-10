package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class LedgerAccountNotFoundException extends ResourceNotFoundException {

    public LedgerAccountNotFoundException(EntityId id) {
        super("Ledger account not found with id: " + id);
    }
}
