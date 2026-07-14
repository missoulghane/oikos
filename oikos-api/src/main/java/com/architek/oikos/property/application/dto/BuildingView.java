package com.architek.oikos.property.application.dto;

import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;

public record BuildingView(BuildingId id, PropertyId propertyId, String name, Integer floorCount) {

    public static BuildingView from(Building building) {
        return new BuildingView(building.getId(), building.getPropertyId(), building.getName(), building.getFloorCount());
    }
}
