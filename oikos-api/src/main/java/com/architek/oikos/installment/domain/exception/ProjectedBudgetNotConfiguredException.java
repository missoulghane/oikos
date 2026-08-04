package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Thrown when generating an installment call for a property in SHARES dues
 * calculation mode whose projected budget has not been configured yet.
 */
public class ProjectedBudgetNotConfiguredException extends BusinessException {

    public ProjectedBudgetNotConfiguredException(EntityId propertyId) {
        super("Property " + propertyId + " is in SHARES dues calculation mode but has no projected budget configured");
    }
}
