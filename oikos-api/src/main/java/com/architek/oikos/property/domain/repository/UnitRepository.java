package com.architek.oikos.property.domain.repository;

import java.util.Optional;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface UnitRepository {

    Unit save(Unit unit);

    Optional<Unit> findById(UnitId id);

    /**
     * Same lookup as {@link #findById}, but takes a pessimistic write lock on
     * the unit row for the duration of the caller's transaction. Callers that
     * are about to add a UnitOwnership for this unit must use this instead of
     * findById, so two concurrent writers targeting the same unit serialize
     * on it rather than both reading a stale ownership-share total.
     */
    Optional<Unit> findByIdForUpdate(UnitId id);

    Page<Unit> findAllByBuildingId(BuildingId buildingId, PageRequest pageRequest);

    /** How many lots the copropriété holds, across all of its buildings. */
    long countByPropertyId(EntityId propertyId);

    boolean existsByUnitTypeId(UnitTypeDefinitionId unitTypeId);
}
