package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.exception.BusinessException;

public class UnitTypeInUseException extends BusinessException {

    public UnitTypeInUseException(UnitTypeDefinitionId id) {
        super("Unit type " + id + " is still assigned to at least one unit and cannot be removed");
    }
}
