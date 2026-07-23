package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a propertyId supplied to accounting (a PROPERTY-type account's
 * holder, or the property resolved from a unit for mirrored bookkeeping)
 * does not correspond to any existing Property. Accounting does not depend on
 * property's own exception type (rule 4) - this is accounting's own view of
 * "reference does not exist". Distinct from
 * property.domain.exception.PropertyNotFoundException, which accounting never
 * depends on directly.
 */
public class PropertyNotFoundException extends ResourceNotFoundException {

    public PropertyNotFoundException(EntityId propertyId) {
        super("No property found with id: " + propertyId);
    }
}
