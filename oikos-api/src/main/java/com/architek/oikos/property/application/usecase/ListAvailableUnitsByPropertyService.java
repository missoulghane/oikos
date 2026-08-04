package com.architek.oikos.property.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ListAvailableUnitsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListAvailableUnitsByPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * Walks every building/unit of a property (paged internally, same
 * small-dataset assumption as ListContactsByPropertyService) and keeps only
 * units with zero UnitOwnership rows - "free" units an invitation can offer
 * to pick from. Pagination applies to the filtered, in-memory result.
 */
@Component
public class ListAvailableUnitsByPropertyService implements ListAvailableUnitsByPropertyUseCase {

    private static final int WALK_PAGE_SIZE = 100;

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public ListAvailableUnitsByPropertyService(PropertyRepository propertyRepository, BuildingRepository buildingRepository,
                                                UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                                                UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitView> listAvailableUnits(ListAvailableUnitsByPropertyQuery query) {
        propertyRepository.findById(query.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(query.propertyId()));

        Map<UnitTypeDefinitionId, String> namesByUnitTypeId = unitTypeDefinitionRepository
                .findAllByPropertyId(query.propertyId()).stream()
                .collect(Collectors.toMap(UnitTypeDefinition::getId, UnitTypeDefinition::getName));

        List<Unit> availableUnits = new ArrayList<>();
        for (Building building : listAllBuildings(query.propertyId())) {
            for (Unit unit : listAllUnits(building.getId())) {
                if (unitOwnershipRepository.findAllByUnitId(unit.getId()).isEmpty()) {
                    availableUnits.add(unit);
                }
            }
        }

        int pageSize = query.pageRequest().pageSize();
        int fromIndex = Math.min(query.pageRequest().pageNumber() * pageSize, availableUnits.size());
        int toIndex = Math.min(fromIndex + pageSize, availableUnits.size());

        List<UnitView> content = availableUnits.subList(fromIndex, toIndex).stream()
                .map(unit -> UnitView.from(unit, false, namesByUnitTypeId.get(unit.getUnitTypeId()), List.of()))
                .toList();

        return Page.of(content, query.pageRequest().pageNumber(), pageSize, availableUnits.size());
    }

    private List<Building> listAllBuildings(PropertyId propertyId) {
        List<Building> buildings = new ArrayList<>();
        int pageNumber = 0;
        Page<Building> page;
        do {
            page = buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(pageNumber, WALK_PAGE_SIZE));
            buildings.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return buildings;
    }

    private List<Unit> listAllUnits(BuildingId buildingId) {
        List<Unit> units = new ArrayList<>();
        int pageNumber = 0;
        Page<Unit> page;
        do {
            page = unitRepository.findAllByBuildingId(buildingId, PageRequest.of(pageNumber, WALK_PAGE_SIZE));
            units.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return units;
    }
}
