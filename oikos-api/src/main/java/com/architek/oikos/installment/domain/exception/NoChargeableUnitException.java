package com.architek.oikos.installment.domain.exception;

import java.time.YearMonth;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

/**
 * Not one unit of the property has an amount to call (no price for its type,
 * or no shares): generating would only produce an empty call that blocks the
 * period - the configuration has to be completed first.
 */
public class NoChargeableUnitException extends BusinessException implements CodedException {

    public NoChargeableUnitException(EntityId propertyId, YearMonth period) {
        super("No unit of property " + propertyId + " has an amount to call for " + period);
    }

    @Override
    public String errorCode() {
        return ErrorCodes.NO_CHARGEABLE_UNIT;
    }
}
