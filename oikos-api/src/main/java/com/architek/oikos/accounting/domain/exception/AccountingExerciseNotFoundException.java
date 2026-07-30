package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class AccountingExerciseNotFoundException extends ResourceNotFoundException {

    public AccountingExerciseNotFoundException(AccountingExerciseId id) {
        super("Accounting exercise not found with id: " + id);
    }
}
