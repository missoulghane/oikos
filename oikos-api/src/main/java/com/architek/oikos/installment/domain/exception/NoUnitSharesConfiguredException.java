package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Thrown when generating an installment call for a property in SHARES dues
 * calculation mode whose units all have zero shares (tantiemes) configured -
 * there is no basis to prorate the projected budget.
 */
public class NoUnitSharesConfiguredException extends BusinessException {

    public NoUnitSharesConfiguredException(EntityId propertyId) {
        super("Property " + propertyId + " is in SHARES dues calculation mode but no unit has shares configured");
    }
}
