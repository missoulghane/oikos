package com.architek.oikos.accounting.domain.exception;

import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.exception.ConflictException;

/** I5: the piece date must fall within the open exercise the entry is attached to. */
public class PieceDateOutsideExerciseException extends ConflictException {

    public PieceDateOutsideExerciseException(LocalDate pieceDate, AccountingExerciseId exerciseId) {
        super("Piece date " + pieceDate + " is outside accounting exercise " + exerciseId);
    }
}
