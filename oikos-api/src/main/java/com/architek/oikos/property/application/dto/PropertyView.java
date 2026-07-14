package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.valueobject.PropertyId;

public record PropertyView(PropertyId id, String name, String address) {

    public static PropertyView from(Property property) {
        return new PropertyView(property.getId(), property.getName(), property.getAddress());
    }
}
