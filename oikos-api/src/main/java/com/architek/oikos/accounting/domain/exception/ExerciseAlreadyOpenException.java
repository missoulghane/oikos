package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/** A property has only one open accounting exercise at a time (spec &sect;3). */
public class ExerciseAlreadyOpenException extends BusinessException {

    public ExerciseAlreadyOpenException(EntityId propertyId) {
        super("An open accounting exercise already exists for property: " + propertyId);
    }
}
