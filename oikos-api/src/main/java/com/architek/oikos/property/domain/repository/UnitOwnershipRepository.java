package com.architek.oikos.property.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface UnitOwnershipRepository {

    UnitOwnership save(UnitOwnership unitOwnership);

    Optional<UnitOwnership> findById(UnitOwnershipId id);

    List<UnitOwnership> findAllByUnitId(UnitId unitId);

    boolean existsByUnitIdAndContactId(UnitId unitId, EntityId contactId);

    void deleteById(UnitOwnershipId id);
}
