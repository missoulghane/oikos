package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitSortField;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

@ExtendWith(MockitoExtension.class)
class ListUnitsByBuildingServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private PartyDirectoryPort partyDirectoryPort;

    private ListUnitsByBuildingService newService() {
        return new ListUnitsByBuildingService(unitRepository, unitOwnershipRepository, unitTypeDefinitionRepository,
                buildingRepository, partyDirectoryPort);
    }

    @Test
    void listing_units_maps_the_repository_page_to_views_with_the_occupation_status() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit unit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.defaultRequest()))
                .thenReturn(Page.of(List.of(unit), 0, 20, 1));
        when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of());

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.defaultRequest(), null);
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A12");
        assertThat(page.content()).extracting(view -> view.unitTypeName()).containsExactly("Appartement");
        assertThat(page.content()).extracting(view -> view.ownershipStatus())
                .containsExactly(OwnershipStatus.NOT_AFFECTED);
    }

    @Test
    void searching_by_owner_keeps_only_units_whose_co_owner_matches_the_name_or_phone() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit matchingUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        Unit otherUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A13", unitTypeId, Shares.of(new BigDecimal("100")));

        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(matchingUnit, otherUnit), 0, 100, 2));

        EntityId matchingPartyId = EntityId.newId();
        EntityId otherPartyId = EntityId.newId();
        when(unitOwnershipRepository.findAllByUnitId(matchingUnit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), matchingUnit.getId(), matchingPartyId, propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));
        when(unitOwnershipRepository.findAllByUnitId(otherUnit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), otherUnit.getId(), otherPartyId, propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));
        when(partyDirectoryPort.getPartyById(matchingPartyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), "0600000000"));
        when(partyDirectoryPort.getPartyById(otherPartyId))
                .thenReturn(new PartyDetails("John Smith", PartyType.INDIVIDUAL, EmailVO.of("john.smith@example.com"), "0611111111"));

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), "jane");
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A12");
        assertThat(page.content()).flatExtracting(view -> view.ownerFullNames()).containsExactly("Jane Doe");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void listing_units_without_search_resolves_the_owner_full_names() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit unit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.defaultRequest()))
                .thenReturn(Page.of(List.of(unit), 0, 20, 1));

        EntityId partyId = EntityId.newId();
        when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), unit.getId(), partyId, propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.defaultRequest(), null);
        var page = newService().listUnits(query);

        assertThat(page.content()).flatExtracting(view -> view.ownerFullNames()).containsExactly("Jane Doe");
        assertThat(page.content()).extracting(view -> view.ownershipStatus()).containsExactly(OwnershipStatus.AFFECTED);
    }

    @Test
    void searching_by_unit_number_keeps_the_lot_even_when_it_has_no_owner() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit matchingUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        Unit otherUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "B07", unitTypeId, Shares.of(new BigDecimal("100")));

        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(matchingUnit, otherUnit), 0, 100, 2));
        when(unitOwnershipRepository.findAllByUnitId(matchingUnit.getId())).thenReturn(List.of());
        when(unitOwnershipRepository.findAllByUnitId(otherUnit.getId())).thenReturn(List.of());

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), "a1");
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A12");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void sorting_by_unit_number_orders_the_digits_as_numbers_not_as_text() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit a10 = Unit.create(UnitId.newId(), buildingId, propertyId, "A10", unitTypeId, Shares.of(new BigDecimal("100")));
        Unit a2 = Unit.create(UnitId.newId(), buildingId, propertyId, "A2", unitTypeId, Shares.of(new BigDecimal("300")));
        Unit a1 = Unit.create(UnitId.newId(), buildingId, propertyId, "A1", unitTypeId, Shares.of(new BigDecimal("200")));
        seedBuilding(propertyId, buildingId, unitTypeId, List.of(a10, a2, a1));

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), null, null,
                UnitSortField.UNIT_NUMBER, SortDirection.ASC);
        var page = newService().listUnits(query);

        // Plain text order would read A1, A10, A2 - the sort a syndic reads as broken.
        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A1", "A2", "A10");
    }

    @Test
    void sorting_by_shares_descending_puts_the_largest_lot_first() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit small = Unit.create(UnitId.newId(), buildingId, propertyId, "A1", unitTypeId, Shares.of(new BigDecimal("100")));
        Unit big = Unit.create(UnitId.newId(), buildingId, propertyId, "A2", unitTypeId, Shares.of(new BigDecimal("300")));
        seedBuilding(propertyId, buildingId, unitTypeId, List.of(small, big));

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), null, null,
                UnitSortField.SHARES, SortDirection.DESC);
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A2", "A1");
    }

    @Test
    void sorting_orders_every_lot_of_the_building_not_only_the_requested_page() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit a3 = Unit.create(UnitId.newId(), buildingId, propertyId, "A3", unitTypeId, Shares.of(new BigDecimal("100")));
        Unit a1 = Unit.create(UnitId.newId(), buildingId, propertyId, "A1", unitTypeId, Shares.of(new BigDecimal("100")));
        Unit a2 = Unit.create(UnitId.newId(), buildingId, propertyId, "A2", unitTypeId, Shares.of(new BigDecimal("100")));
        seedBuilding(propertyId, buildingId, unitTypeId, List.of(a3, a1, a2));

        // Page 1 of size 1: only a sort applied before paginating can put A2 here.
        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(1, 1), null, null,
                UnitSortField.UNIT_NUMBER, SortDirection.ASC);
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A2");
        assertThat(page.totalElements()).isEqualTo(3);
    }

    /** Building, unit types and ownership-free units - the shape every sort test needs. */
    private void seedBuilding(PropertyId propertyId, BuildingId buildingId, UnitTypeDefinitionId unitTypeId,
                               List<Unit> units) {
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(units, 0, 100, units.size()));
        units.forEach(unit -> when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of()));
    }

    @Test
    void filtering_on_not_affected_keeps_only_the_units_without_any_ownership() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit affectedUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        Unit freeUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A13", unitTypeId, Shares.of(new BigDecimal("100")));

        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(affectedUnit, freeUnit), 0, 100, 2));

        EntityId partyId = EntityId.newId();
        when(unitOwnershipRepository.findAllByUnitId(affectedUnit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), affectedUnit.getId(), partyId, propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));
        when(unitOwnershipRepository.findAllByUnitId(freeUnit.getId())).thenReturn(List.of());

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), null, OwnershipStatus.NOT_AFFECTED);
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A13");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void filtering_on_affected_combines_with_the_search_on_the_same_page() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit affectedUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        Unit freeUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A13", unitTypeId, Shares.of(new BigDecimal("100")));

        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(affectedUnit, freeUnit), 0, 100, 2));

        EntityId partyId = EntityId.newId();
        when(unitOwnershipRepository.findAllByUnitId(affectedUnit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), affectedUnit.getId(), partyId, propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));
        when(unitOwnershipRepository.findAllByUnitId(freeUnit.getId())).thenReturn(List.of());
        when(partyDirectoryPort.getPartyById(partyId))
                .thenReturn(new PartyDetails("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null));

        // "A1" matches both lot numbers, the status filter is what narrows it down.
        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.of(0, 20), "A1", OwnershipStatus.AFFECTED);
        var page = newService().listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A12");
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
