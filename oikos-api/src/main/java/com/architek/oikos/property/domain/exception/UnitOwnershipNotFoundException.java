package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class UnitOwnershipNotFoundException extends ResourceNotFoundException {

    public UnitOwnershipNotFoundException(UnitOwnershipId id) {
        super("UnitOwnership not found with id: " + id);
    }
}
