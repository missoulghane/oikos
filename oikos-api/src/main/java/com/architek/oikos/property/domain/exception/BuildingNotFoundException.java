package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class BuildingNotFoundException extends ResourceNotFoundException {

    public BuildingNotFoundException(BuildingId id) {
        super("Building not found with id: " + id);
    }
}
