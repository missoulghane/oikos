package com.architek.oikos.accounting.domain.exception;

import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** I5: the exercise covering the piece date exists but is closed, hence read-only. */
public class PieceDateInClosedExerciseException extends ConflictException implements CodedException {

    public PieceDateInClosedExerciseException(LocalDate pieceDate, AccountingExerciseId exerciseId) {
        super("Piece date " + pieceDate + " falls in closed accounting exercise " + exerciseId);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.EXERCISE_CLOSED_FOR_DATE;
    }
}
