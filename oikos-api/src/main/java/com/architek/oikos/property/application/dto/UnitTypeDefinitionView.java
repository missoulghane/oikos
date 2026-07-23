package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public record UnitTypeDefinitionView(UnitTypeDefinitionId id, PropertyId propertyId, String name) {

    public static UnitTypeDefinitionView from(UnitTypeDefinition unitTypeDefinition) {
        return new UnitTypeDefinitionView(unitTypeDefinition.getId(), unitTypeDefinition.getPropertyId(),
                unitTypeDefinition.getName());
    }
}
