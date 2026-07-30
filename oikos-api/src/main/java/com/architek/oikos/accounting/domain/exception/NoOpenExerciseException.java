package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/** Every new accounting operation must be attached to an open exercise (spec &sect;3). */
public class NoOpenExerciseException extends BusinessException {

    public NoOpenExerciseException(EntityId propertyId) {
        super("No open accounting exercise for property: " + propertyId);
    }
}
