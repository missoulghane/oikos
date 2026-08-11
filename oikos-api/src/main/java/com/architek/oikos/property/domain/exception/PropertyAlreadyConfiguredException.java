package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * The onboarding wizard tried to lay out a property that already has
 * buildings. Guarding on "has buildings" rather than on a flag is what makes
 * the endpoint safe to retry: a double submit (or a resumed wizard) leaves the
 * first structure intact instead of duplicating every lot.
 */
public class PropertyAlreadyConfiguredException extends ConflictException {

    public PropertyAlreadyConfiguredException(PropertyId propertyId) {
        super("Property " + propertyId + " already has buildings and cannot be configured again");
    }
}
