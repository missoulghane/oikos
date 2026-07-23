package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class UnitTypeNameAlreadyUsedException extends BusinessException {

    public UnitTypeNameAlreadyUsedException(String name) {
        super("A unit type named '" + name + "' already exists for this property");
    }
}
