package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;

public record AddUnitTypeDefinitionCommand(PropertyId propertyId, String name) {
}
