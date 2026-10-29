package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** Exercises are closed in order: N+1 cannot be closed while N is still open. */
public class ExerciseNotOldestOpenException extends ConflictException implements CodedException {

    public ExerciseNotOldestOpenException(AccountingExerciseId exerciseId, AccountingExerciseId olderOpenId) {
        super("Accounting exercise " + exerciseId + " cannot be closed before the older open exercise "
                + olderOpenId);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.EXERCISE_NOT_OLDEST_OPEN;
    }
}
