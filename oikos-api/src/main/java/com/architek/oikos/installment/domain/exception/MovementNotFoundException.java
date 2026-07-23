package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a movementId referenced by a manual allocation does not
 * correspond to any existing Movement (accounting's ledger). Distinct from
 * accounting's own MovementNotFoundException, which installment never depends
 * on directly (rule 4).
 */
public class MovementNotFoundException extends ResourceNotFoundException {

    public MovementNotFoundException(EntityId movementId) {
        super("Movement not found with id: " + movementId);
    }
}
