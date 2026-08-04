package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * search filters units by their owner's (any co-owner's) full name or phone
 * (case-insensitive substring); null means no filter on this axis.
 */
public record ListUnitsByBuildingQuery(BuildingId buildingId, PageRequest pageRequest, String search) {
}
