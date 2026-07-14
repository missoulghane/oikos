package com.architek.oikos.property.domain.repository;

import java.util.Optional;

import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface BuildingRepository {

    Building save(Building building);

    Optional<Building> findById(BuildingId id);

    Page<Building> findAllByPropertyId(PropertyId propertyId, PageRequest pageRequest);
}
