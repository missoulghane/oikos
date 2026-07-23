package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.UnitTypePriceView;

public record UnitTypePriceResponse(String id, String propertyId, String unitTypeId, String unitTypeName,
                                    BigDecimal price) {

    public static UnitTypePriceResponse from(UnitTypePriceView view) {
        return new UnitTypePriceResponse(view.id().toString(), view.propertyId().toString(),
                view.unitTypeId().toString(), view.unitTypeName(), view.price().value());
    }
}
