package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListUnitsByBuildingService implements ListUnitsByBuildingUseCase {

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;

    public ListUnitsByBuildingService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitView> listUnits(ListUnitsByBuildingQuery query) {
        return unitRepository.findAllByBuildingId(query.buildingId(), query.pageRequest())
                .map(unit -> UnitView.from(unit, !unitOwnershipRepository.findAllByUnitId(unit.getId()).isEmpty()));
    }
}
