package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListUnitsByBuildingService implements ListUnitsByBuildingUseCase {

    private static final int PAGE_SIZE = 100;

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final BuildingRepository buildingRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public ListUnitsByBuildingService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                                       UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                       BuildingRepository buildingRepository, PartyDirectoryPort partyDirectoryPort) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.buildingRepository = buildingRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitView> listUnits(ListUnitsByBuildingQuery query) {
        Building building = buildingRepository.findById(query.buildingId())
                .orElseThrow(() -> new BuildingNotFoundException(query.buildingId()));
        Map<UnitTypeDefinitionId, String> namesByUnitTypeId = unitTypeDefinitionRepository
                .findAllByPropertyId(building.getPropertyId()).stream()
                .collect(Collectors.toMap(UnitTypeDefinition::getId, UnitTypeDefinition::getName));

        boolean searches = query.search() != null && !query.search().isBlank();
        if (!searches && query.ownershipStatus() == null) {
            Map<EntityId, PartyDetails> partyDetailsCache = new HashMap<>();
            return unitRepository.findAllByBuildingId(query.buildingId(), query.pageRequest())
                    .map(unit -> toView(unit, unitOwnershipRepository.findAllByUnitId(unit.getId()),
                            namesByUnitTypeId, partyDetailsCache));
        }

        return filterInMemory(query, namesByUnitTypeId);
    }

    /**
     * Both filters need data the unit table alone cannot answer - the owners'
     * name/phone lives behind PartyDirectoryPort (never joined directly, rule
     * 6) and the ownership status is derived from UnitOwnership - so matching
     * happens in memory over every unit of the building, same
     * in-memory-filter assumption as ListContactsByPropertyService.
     */
    private Page<UnitView> filterInMemory(ListUnitsByBuildingQuery query, Map<UnitTypeDefinitionId, String> namesByUnitTypeId) {
        String pattern = query.search() == null || query.search().isBlank()
                ? null
                : query.search().trim().toLowerCase(Locale.ROOT);
        List<Unit> allUnits = listAllUnits(query.buildingId());

        Map<UnitId, List<UnitOwnership>> ownershipsByUnitId = allUnits.stream()
                .collect(Collectors.toMap(Unit::getId, unit -> unitOwnershipRepository.findAllByUnitId(unit.getId())));
        Map<EntityId, PartyDetails> partyDetailsCache = new HashMap<>();

        List<Unit> matchingUnits = allUnits.stream()
                .filter(unit -> matchesStatus(ownershipsByUnitId.get(unit.getId()), query.ownershipStatus()))
                .filter(unit -> matchesSearch(unit, ownershipsByUnitId.get(unit.getId()), pattern, partyDetailsCache))
                .toList();

        int pageSize = query.pageRequest().pageSize();
        int fromIndex = Math.min(query.pageRequest().pageNumber() * pageSize, matchingUnits.size());
        int toIndex = Math.min(fromIndex + pageSize, matchingUnits.size());

        List<UnitView> content = matchingUnits.subList(fromIndex, toIndex).stream()
                .map(unit -> toView(unit, ownershipsByUnitId.get(unit.getId()), namesByUnitTypeId, partyDetailsCache))
                .toList();

        return Page.of(content, query.pageRequest().pageNumber(), pageSize, matchingUnits.size());
    }

    private UnitView toView(Unit unit, List<UnitOwnership> ownerships, Map<UnitTypeDefinitionId, String> namesByUnitTypeId,
                             Map<EntityId, PartyDetails> partyDetailsCache) {
        List<String> ownerFullNames = ownerships.stream()
                .map(ownership -> partyDetails(ownership.getPartyId(), partyDetailsCache).fullName())
                .toList();
        return UnitView.from(unit, !ownerships.isEmpty(), namesByUnitTypeId.get(unit.getUnitTypeId()), ownerFullNames);
    }

    private PartyDetails partyDetails(EntityId partyId, Map<EntityId, PartyDetails> cache) {
        return cache.computeIfAbsent(partyId, partyDirectoryPort::getPartyById);
    }

    /** RG-LOT-01 again, but on the query side: no ownership at all means NOT_AFFECTED. */
    private static boolean matchesStatus(List<UnitOwnership> ownerships, OwnershipStatus wanted) {
        if (wanted == null) {
            return true;
        }
        OwnershipStatus actual = ownerships.isEmpty() ? OwnershipStatus.NOT_AFFECTED : OwnershipStatus.AFFECTED;
        return actual == wanted;
    }

    /**
     * A lot is searchable both by its own number (the flat number displayed as
     * its name) and by any of its co-owners' full name or phone.
     */
    private boolean matchesSearch(Unit unit, List<UnitOwnership> ownerships, String pattern,
                                   Map<EntityId, PartyDetails> partyDetailsCache) {
        if (pattern == null) {
            return true;
        }
        if (unit.getUnitNumber() != null && unit.getUnitNumber().toLowerCase(Locale.ROOT).contains(pattern)) {
            return true;
        }
        return ownerships.stream()
                .map(ownership -> partyDetails(ownership.getPartyId(), partyDetailsCache))
                .anyMatch(partyDetails -> matches(partyDetails, pattern));
    }

    private static boolean matches(PartyDetails partyDetails, String pattern) {
        boolean nameMatches = partyDetails.fullName() != null
                && partyDetails.fullName().toLowerCase(Locale.ROOT).contains(pattern);
        boolean phoneMatches = partyDetails.phone() != null
                && partyDetails.phone().toLowerCase(Locale.ROOT).contains(pattern);
        return nameMatches || phoneMatches;
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
