package com.architek.oikos.property.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
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
 * pagination on that walk, since a property's unit count stays small enough
 * to hold in memory (same assumption as InstallmentPropertyDirectoryAdapter).
 * The query's own pagination and search apply on top of that in-memory list,
 * grouped by party: a "page" is a page of distinct parties (contact groups),
 * and content is the flattened unit-ownership rows for the parties on that
 * page - never a party split across two pages.
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
    public Page<PropertyContactView> listContacts(ListContactsByPropertyQuery query) {
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

        Map<EntityId, List<UnitOwnership>> ownershipsByPartyId = unitOwnerships.stream()
                .collect(Collectors.groupingBy(UnitOwnership::getPartyId, LinkedHashMap::new, Collectors.toList()));

        Map<EntityId, PartyDetails> partyDetailsById = new LinkedHashMap<>();
        for (EntityId partyId : ownershipsByPartyId.keySet()) {
            partyDetailsById.put(partyId, partyDirectoryPort.getPartyById(partyId));
        }

        List<EntityId> matchingPartyIds = partyDetailsById.entrySet().stream()
                .filter(entry -> matchesSearch(entry.getValue(), query.search()))
                .sorted(Comparator.comparing(entry -> entry.getValue().fullName(), String.CASE_INSENSITIVE_ORDER))
                .map(Map.Entry::getKey)
                .toList();

        int pageSize = query.pageRequest().pageSize();
        int fromIndex = Math.min(query.pageRequest().pageNumber() * pageSize, matchingPartyIds.size());
        int toIndex = Math.min(fromIndex + pageSize, matchingPartyIds.size());
        List<EntityId> pagePartyIds = matchingPartyIds.subList(fromIndex, toIndex);

        List<PropertyContactView> content = pagePartyIds.stream()
                .flatMap(partyId -> ownershipsByPartyId.get(partyId).stream()
                        .map(unitOwnership -> {
                            UnitInfo unitInfo = unitInfoById.get(unitOwnership.getUnitId());
                            return PropertyContactView.from(unitOwnership, partyDetailsById.get(partyId),
                                    unitInfo.unitNumber(), unitInfo.buildingName(), linkedPartyIds.contains(partyId));
                        }))
                .toList();

        return Page.of(content, query.pageRequest().pageNumber(), pageSize, matchingPartyIds.size());
    }

    private static boolean matchesSearch(PartyDetails partyDetails, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String pattern = search.trim().toLowerCase(Locale.ROOT);
        boolean nameMatches = partyDetails.fullName() != null
                && partyDetails.fullName().toLowerCase(Locale.ROOT).contains(pattern);
        boolean phoneMatches = partyDetails.phone() != null
                && partyDetails.phone().toLowerCase(Locale.ROOT).contains(pattern);
        return nameMatches || phoneMatches;
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
