package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Only a CREDIT movement can be allocated to an installment.
 */
public class MovementNotCreditException extends BusinessException {

    public MovementNotCreditException(EntityId id) {
        super("Movement " + id + " is not a credit movement and cannot be allocated");
    }
}
