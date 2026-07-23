package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class MovementNotFoundException extends ResourceNotFoundException {

    public MovementNotFoundException(MovementId id) {
        super("Movement not found with id: " + id);
    }
}
