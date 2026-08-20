package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.BuildingId;

public record UpdateBuildingCommand(BuildingId id, String name, Integer floorCount) {
}
