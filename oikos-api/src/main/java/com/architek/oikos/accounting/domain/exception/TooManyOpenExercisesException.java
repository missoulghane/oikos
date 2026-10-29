package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/**
 * Two exercises open at once is the overlap window (N awaiting its closing, N+1
 * already in use); a third would mean N has been left open for over a year.
 */
public class TooManyOpenExercisesException extends ConflictException implements CodedException {

    public TooManyOpenExercisesException(EntityId propertyId, int maxOpen) {
        super("Property " + propertyId + " already has " + maxOpen + " open accounting exercises");
    }

    @Override
    public String errorCode() {
        return ErrorCodes.TOO_MANY_OPEN_EXERCISES;
    }
}
