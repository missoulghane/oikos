package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public record RemoveUnitTypePriceCommand(PropertyId propertyId, UnitTypeDefinitionId unitTypeId) {
}
