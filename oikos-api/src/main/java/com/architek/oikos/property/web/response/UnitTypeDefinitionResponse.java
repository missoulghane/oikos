package com.architek.oikos.property.web.response;

import com.architek.oikos.property.application.dto.UnitTypeDefinitionView;

public record UnitTypeDefinitionResponse(String id, String propertyId, String name) {

    public static UnitTypeDefinitionResponse from(UnitTypeDefinitionView view) {
        return new UnitTypeDefinitionResponse(view.id().toString(), view.propertyId().toString(), view.name());
    }
}
