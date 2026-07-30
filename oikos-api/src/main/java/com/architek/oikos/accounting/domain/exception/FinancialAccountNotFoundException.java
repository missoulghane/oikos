package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class FinancialAccountNotFoundException extends ResourceNotFoundException {

    public FinancialAccountNotFoundException(FinancialAccountId id) {
        super("Financial account not found with id: " + id);
    }
}
