package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListUnitsByBuildingQuery(BuildingId buildingId, PageRequest pageRequest) {
}
