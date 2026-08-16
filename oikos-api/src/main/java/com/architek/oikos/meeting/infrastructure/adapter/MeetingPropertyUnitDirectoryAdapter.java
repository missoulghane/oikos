package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: walks the copropriété through property's public
 * port-in use cases only (rule 4/6), never its repositories.
 *
 * <p>The walk starts from the buildings and their lots, not from the ownership
 * records, and that is the whole point: a lot nobody owns yet has no ownership
 * row, and listing contacts would silently drop it from the convocation - and
 * with it, its weight in the totals the quorum and an absolute majority are
 * measured against. Owners are then layered on top, and a lot without any is a
 * lot with an empty owner list, not an absent lot.
 *
 * <p>Everything is paged through in full: this is one copropriété's lot
 * registry, the same assumption ListContactsByPropertyService already makes
 * internally.
 */
@Component
public class MeetingPropertyUnitDirectoryAdapter implements PropertyUnitDirectoryPort {

    private static final int PAGE_SIZE = 100;

    private final ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase;
    private final ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;
    private final ListContactsByPropertyUseCase listContactsByPropertyUseCase;

    public MeetingPropertyUnitDirectoryAdapter(ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase,
                                                ListUnitsByBuildingUseCase listUnitsByBuildingUseCase,
                                                ListContactsByPropertyUseCase listContactsByPropertyUseCase) {
        this.listBuildingsByPropertyUseCase = listBuildingsByPropertyUseCase;
        this.listUnitsByBuildingUseCase = listUnitsByBuildingUseCase;
        this.listContactsByPropertyUseCase = listContactsByPropertyUseCase;
    }

    @Override
    public List<UnitInfo> listUnits(EntityId propertyId) {
        PropertyId typedPropertyId = PropertyId.of(propertyId.value());
        Map<EntityId, List<OwnerInfo>> ownersByUnit = ownersByUnit(typedPropertyId);

        List<UnitInfo> units = new ArrayList<>();
        for (BuildingView building : allBuildings(typedPropertyId)) {
            for (UnitView unit : allUnits(building)) {
                EntityId unitId = EntityId.of(unit.id().value().value());
                units.add(new UnitInfo(unitId, unit.unitNumber(), building.name(), unit.shares(),
                        ownersByUnit.getOrDefault(unitId, List.of())));
            }
        }
        return units;
    }

    private Map<EntityId, List<OwnerInfo>> ownersByUnit(PropertyId propertyId) {
        Map<EntityId, List<OwnerInfo>> byUnit = new LinkedHashMap<>();
        int pageNumber = 0;
        Page<PropertyContactView> page;
        do {
            page = listContactsByPropertyUseCase.listContacts(
                    new ListContactsByPropertyQuery(propertyId, PageRequest.of(pageNumber, PAGE_SIZE), null));
            for (PropertyContactView contact : page.content()) {
                byUnit.computeIfAbsent(EntityId.of(contact.unitId().value().value()), key -> new ArrayList<>())
                        .add(new OwnerInfo(contact.partyId(), contact.partyFullName(), contact.partyEmail()));
            }
            pageNumber++;
        } while (page.hasNext());
        return byUnit;
    }

    private List<BuildingView> allBuildings(PropertyId propertyId) {
        List<BuildingView> buildings = new ArrayList<>();
        int pageNumber = 0;
        Page<BuildingView> page;
        do {
            page = listBuildingsByPropertyUseCase.listBuildings(
                    new ListBuildingsByPropertyQuery(propertyId, PageRequest.of(pageNumber, PAGE_SIZE)));
            buildings.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return buildings;
    }

    private List<UnitView> allUnits(BuildingView building) {
        List<UnitView> units = new ArrayList<>();
        int pageNumber = 0;
        Page<UnitView> page;
        do {
            page = listUnitsByBuildingUseCase.listUnits(
                    new ListUnitsByBuildingQuery(building.id(), PageRequest.of(pageNumber, PAGE_SIZE), null));
            units.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return units;
    }
}
