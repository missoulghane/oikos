package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.exception.BusinessException;

/** A closed exercise is entirely read-only (spec &sect;3/&sect;17). */
public class AccountingExerciseClosedException extends BusinessException {

    public AccountingExerciseClosedException(AccountingExerciseId id) {
        super("Accounting exercise is closed and read-only: " + id);
    }
}
