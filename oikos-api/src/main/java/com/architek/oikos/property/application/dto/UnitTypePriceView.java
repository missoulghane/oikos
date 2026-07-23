package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

public record UnitTypePriceView(UnitTypePricingId id, PropertyId propertyId, UnitTypeDefinitionId unitTypeId,
                                String unitTypeName, Price price) {

    public static UnitTypePriceView from(UnitTypePricing unitTypePricing, String unitTypeName) {
        return new UnitTypePriceView(unitTypePricing.getId(), unitTypePricing.getPropertyId(),
                unitTypePricing.getUnitTypeId(), unitTypeName, unitTypePricing.getPrice());
    }
}
