package com.architek.oikos.property.domain.repository;

import java.util.Optional;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface UnitRepository {

    Unit save(Unit unit);

    Optional<Unit> findById(UnitId id);

    Page<Unit> findAllByBuildingId(BuildingId buildingId, PageRequest pageRequest);
}
