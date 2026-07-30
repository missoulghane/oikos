package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class UnitAccountNotFoundException extends ResourceNotFoundException {

    public UnitAccountNotFoundException(UnitAccountId id) {
        super("Unit account not found with id: " + id);
    }

    public static UnitAccountNotFoundException forUnit(EntityId unitId) {
        return new UnitAccountNotFoundException("Unit account not found for unit: " + unitId);
    }

    private UnitAccountNotFoundException(String message) {
        super(message);
    }
}
