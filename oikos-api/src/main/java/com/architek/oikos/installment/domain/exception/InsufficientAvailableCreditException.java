package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

public class InsufficientAvailableCreditException extends BusinessException {

    public InsufficientAvailableCreditException(EntityId movementId) {
        super("Movement " + movementId + " does not have enough unallocated credit for this allocation");
    }
}
