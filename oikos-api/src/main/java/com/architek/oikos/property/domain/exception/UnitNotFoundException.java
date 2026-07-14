package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class UnitNotFoundException extends ResourceNotFoundException {

    public UnitNotFoundException(UnitId id) {
        super("Unit not found with id: " + id);
    }
}
