package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class UnitTypePriceNotFoundException extends ResourceNotFoundException {

    public UnitTypePriceNotFoundException(UnitTypeDefinitionId unitTypeId) {
        super("No price configured for unit type " + unitTypeId);
    }
}
