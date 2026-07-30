package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface UnitAccountMovementRepository {

    UnitAccountMovement save(UnitAccountMovement movement);

    Optional<UnitAccountMovement> findById(UnitAccountMovementId id);

    Page<UnitAccountMovement> findPageByUnitAccountId(UnitAccountId unitAccountId, PageRequest pageRequest);

    /** Unpaginated: used to aggregate a single lot's totals (spec &sect;16 "Units") - naturally small per unit. */
    List<UnitAccountMovement> findAllByUnitAccountId(UnitAccountId unitAccountId);
}
