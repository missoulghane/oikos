package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a unitId supplied to a installment call line does not
 * correspond to any existing Unit. Distinct from
 * property.domain.exception.UnitNotFoundException, which installment does not
 * depend on directly (rule 4).
 */
public class UnitNotFoundException extends ResourceNotFoundException {

    public UnitNotFoundException(EntityId unitId) {
        super("No unit found with id: " + unitId);
    }
}
