package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a propertyId supplied to accounting (e.g. to open an exercise
 * or create a financial account) does not correspond to any existing
 * Property. accounting does not depend on property's own exception type
 * (rule 4) - this is accounting's own view of "reference does not exist".
 */
public class PropertyNotFoundException extends ResourceNotFoundException {

    public PropertyNotFoundException(EntityId propertyId) {
        super("No property found with id: " + propertyId);
    }
}
