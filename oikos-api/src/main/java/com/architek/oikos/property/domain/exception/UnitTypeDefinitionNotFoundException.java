package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class UnitTypeDefinitionNotFoundException extends ResourceNotFoundException {

    public UnitTypeDefinitionNotFoundException(UnitTypeDefinitionId id) {
        super("UnitTypeDefinition not found with id: " + id);
    }
}
