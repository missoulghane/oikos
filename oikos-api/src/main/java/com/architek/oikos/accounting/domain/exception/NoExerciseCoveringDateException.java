package com.architek.oikos.accounting.domain.exception;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** I5: no exercise of the property covers the piece date - it has to be opened first. */
public class NoExerciseCoveringDateException extends ConflictException implements CodedException {

    public NoExerciseCoveringDateException(EntityId propertyId, LocalDate pieceDate) {
        super("No accounting exercise of property " + propertyId + " covers piece date " + pieceDate);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.NO_EXERCISE_FOR_DATE;
    }
}
