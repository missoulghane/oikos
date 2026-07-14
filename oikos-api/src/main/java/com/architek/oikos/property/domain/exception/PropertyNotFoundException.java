package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class PropertyNotFoundException extends ResourceNotFoundException {

    public PropertyNotFoundException(PropertyId id) {
        super("Property not found with id: " + id);
    }
}
