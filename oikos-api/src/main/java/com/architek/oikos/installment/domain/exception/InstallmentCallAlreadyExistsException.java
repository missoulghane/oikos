package com.architek.oikos.installment.domain.exception;

import java.time.YearMonth;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

public class InstallmentCallAlreadyExistsException extends BusinessException {

    public InstallmentCallAlreadyExistsException(EntityId propertyId, YearMonth period) {
        super("A installment call already exists for property " + propertyId + " and period " + period);
    }
}
