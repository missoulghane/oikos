package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListUnitsByBuildingService implements ListUnitsByBuildingUseCase {

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final BuildingRepository buildingRepository;

    public ListUnitsByBuildingService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                                       UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                       BuildingRepository buildingRepository) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitView> listUnits(ListUnitsByBuildingQuery query) {
        Building building = buildingRepository.findById(query.buildingId())
                .orElseThrow(() -> new BuildingNotFoundException(query.buildingId()));
        Map<UnitTypeDefinitionId, String> namesByUnitTypeId = unitTypeDefinitionRepository
                .findAllByPropertyId(building.getPropertyId()).stream()
                .collect(Collectors.toMap(UnitTypeDefinition::getId, UnitTypeDefinition::getName));

        return unitRepository.findAllByBuildingId(query.buildingId(), query.pageRequest())
                .map(unit -> UnitView.from(unit, !unitOwnershipRepository.findAllByUnitId(unit.getId()).isEmpty(),
                        namesByUnitTypeId.get(unit.getUnitTypeId())));
    }
}
