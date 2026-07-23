package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a unitId supplied to a installment call line does not
 * correspond to any existing Unit. Accounting does not depend on property's own
 * exception type (rule 4) - this is accounting's own view of "reference does
 * not exist". Distinct from property.domain.exception.UnitNotFoundException,
 * which accounting never depends on directly.
 */
public class UnitNotFoundException extends ResourceNotFoundException {

    public UnitNotFoundException(EntityId unitId) {
        super("No unit found with id: " + unitId);
    }
}
