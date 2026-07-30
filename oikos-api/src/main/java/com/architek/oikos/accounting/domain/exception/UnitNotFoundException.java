package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a unitId supplied to accounting (e.g. to provision its unit
 * account) does not correspond to any existing Unit. Distinct from
 * property.domain.exception.UnitNotFoundException, which accounting does not
 * depend on directly (rule 4).
 */
public class UnitNotFoundException extends ResourceNotFoundException {

    public UnitNotFoundException(EntityId unitId) {
        super("No unit found with id: " + unitId);
    }
}
