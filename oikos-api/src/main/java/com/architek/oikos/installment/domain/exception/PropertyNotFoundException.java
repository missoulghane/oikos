package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a propertyId supplied to installment (a installment call's
 * property, or the property resolved from a unit) does not correspond to any
 * existing Property. Installment does not depend on property's own exception
 * type (rule 4) - this is installment's own view of "reference does not
 * exist". Distinct from accounting's own PropertyNotFoundException and from
 * property.domain.exception.PropertyNotFoundException, neither of which
 * installment depends on directly.
 */
public class PropertyNotFoundException extends ResourceNotFoundException {

    public PropertyNotFoundException(EntityId propertyId) {
        super("No property found with id: " + propertyId);
    }
}
