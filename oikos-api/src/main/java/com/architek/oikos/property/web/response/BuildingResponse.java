package com.architek.oikos.property.web.response;

import com.architek.oikos.property.application.dto.BuildingView;

public record BuildingResponse(String id, String propertyId, String name, Integer floorCount) {

    public static BuildingResponse from(BuildingView view) {
        return new BuildingResponse(view.id().toString(), view.propertyId().toString(), view.name(), view.floorCount());
    }
}
