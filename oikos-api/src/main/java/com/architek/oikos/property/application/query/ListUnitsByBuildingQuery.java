package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitSortField;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;

/**
 * search filters units by their own number (lot / numero d'appartement) or by
 * their owner's (any co-owner's) full name or phone (case-insensitive
 * substring); ownershipStatus keeps only the affected or only the unaffected
 * units (RG-LOT-01). null means no filter on this axis, and the two axes
 * combine (AND) when both are set.
 *
 * sortField null keeps the repository's own order; naming one switches the
 * listing to the in-memory path, so a sort orders every lot of the building
 * rather than reshuffling the rows of the current page.
 */
public record ListUnitsByBuildingQuery(BuildingId buildingId, PageRequest pageRequest, String search,
                                       OwnershipStatus ownershipStatus, UnitSortField sortField,
                                       SortDirection sortDirection) {

    public ListUnitsByBuildingQuery {
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
    }

    /** Unfiltered-on-status listing, for the callers that only search (or not at all). */
    public ListUnitsByBuildingQuery(BuildingId buildingId, PageRequest pageRequest, String search) {
        this(buildingId, pageRequest, search, null, null, null);
    }

    public ListUnitsByBuildingQuery(BuildingId buildingId, PageRequest pageRequest, String search,
                                     OwnershipStatus ownershipStatus) {
        this(buildingId, pageRequest, search, ownershipStatus, null, null);
    }
}
