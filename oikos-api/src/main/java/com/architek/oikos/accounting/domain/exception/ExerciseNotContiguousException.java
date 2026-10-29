package com.architek.oikos.accounting.domain.exception;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** A property's exercises follow one another with no gap and no overlap. */
public class ExerciseNotContiguousException extends ConflictException implements CodedException {

    public ExerciseNotContiguousException(EntityId propertyId, LocalDate expectedStartDate, LocalDate startDate) {
        super("The next accounting exercise of property " + propertyId + " must start on " + expectedStartDate
                + ", not " + startDate);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.EXERCISE_NOT_CONTIGUOUS;
    }
}
