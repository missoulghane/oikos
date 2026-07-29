package com.architek.oikos.property.application.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Lists every contact (party, via UnitOwnership) attached to any lot of a
 * property, for the "Contacts" tab of the property's own page. Buildings and
 * units are walked in full (paging internally) rather than exposing
 * pagination here, since a property's unit count stays small enough to hold
 * in memory (same assumption as InstallmentPropertyDirectoryAdapter).
 */
@Component
public class ListContactsByPropertyService implements ListContactsByPropertyUseCase {

    private static final int PAGE_SIZE = 100;

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AccountLinkingPort accountLinkingPort;

    public ListContactsByPropertyService(PropertyRepository propertyRepository, BuildingRepository buildingRepository,
                                          UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                                          PartyDirectoryPort partyDirectoryPort, AccountLinkingPort accountLinkingPort) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.accountLinkingPort = accountLinkingPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyContactView> listContacts(ListContactsByPropertyQuery query) {
        propertyRepository.findById(query.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(query.propertyId()));

        record UnitInfo(String unitNumber, String buildingName) {
        }

        Map<UnitId, UnitInfo> unitInfoById = new HashMap<>();
        for (Building building : listAllBuildings(query.propertyId())) {
            for (Unit unit : listAllUnits(building.getId())) {
                unitInfoById.put(unit.getId(), new UnitInfo(unit.getUnitNumber(), building.getName()));
            }
        }

        List<UnitOwnership> unitOwnerships = unitOwnershipRepository.findAllByUnitIds(List.copyOf(unitInfoById.keySet()));

        Set<EntityId> linkedPartyIds = accountLinkingPort.findLinkedPartyIds(
                unitOwnerships.stream().map(UnitOwnership::getPartyId).toList());

        return unitOwnerships.stream()
                .map(unitOwnership -> {
                    UnitInfo unitInfo = unitInfoById.get(unitOwnership.getUnitId());
                    return PropertyContactView.from(unitOwnership, partyDirectoryPort.getPartyById(unitOwnership.getPartyId()),
                            unitInfo.unitNumber(), unitInfo.buildingName(),
                            linkedPartyIds.contains(unitOwnership.getPartyId()));
                })
                .toList();
    }

    private List<Building> listAllBuildings(PropertyId propertyId) {
        List<Building> buildings = new ArrayList<>();
        int pageNumber = 0;
        Page<Building> page;
        do {
            page = buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(pageNumber, PAGE_SIZE));
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
            page = unitRepository.findAllByBuildingId(buildingId, PageRequest.of(pageNumber, PAGE_SIZE));
            units.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return units;
    }
}
