package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.port.out.PropertyDuesConfigurationView;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.installment.application.port.out.UnitShareLine;
import com.architek.oikos.installment.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypePricesByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentPropertyDirectoryAdapterTest {

    @Mock
    private ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase;

    @Mock
    private ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;

    @Mock
    private GetPropertyUseCase getPropertyUseCase;

    @Mock
    private ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase;

    @Mock
    private ListPropertiesUseCase listPropertiesUseCase;

    private InstallmentPropertyDirectoryAdapter newAdapter() {
        return new InstallmentPropertyDirectoryAdapter(listBuildingsByPropertyUseCase, listUnitsByBuildingUseCase,
                getPropertyUseCase, listUnitTypePricesByPropertyUseCase, listPropertiesUseCase);
    }

    private static PropertyView propertyView(PropertyId id) {
        return new PropertyView(id, "Residence " + id, "1 rue Test", "Casablanca",
                com.architek.oikos.property.domain.valueobject.DuesCalculationMode.FLAT_RATE, null);
    }

    /**
     * The nightly imputation sweep reads every copropriété through this method,
     * and the list use case is paginated: stopping at the first page would leave
     * every property beyond it unregularized, silently.
     */
    @Test
    void list_all_ids_walks_every_page() {
        PropertyId first = PropertyId.newId();
        PropertyId second = PropertyId.newId();
        when(listPropertiesUseCase.listProperties(new ListPropertiesQuery(PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(propertyView(first)), 0, 100, 101));
        when(listPropertiesUseCase.listProperties(new ListPropertiesQuery(PageRequest.of(1, 100))))
                .thenReturn(Page.of(List.of(propertyView(second)), 1, 100, 101));

        assertThat(newAdapter().listAllIds()).containsExactly(first.value(), second.value());
    }

    @Test
    void exists_returns_true_when_the_property_is_found() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        when(getPropertyUseCase.getProperty(new GetPropertyQuery(propertyIdValue)))
                .thenReturn(new PropertyView(propertyIdValue, "Residence", "1 rue Test", "Casablanca",
                        com.architek.oikos.property.domain.valueobject.DuesCalculationMode.FLAT_RATE, null));

        boolean exists = newAdapter().exists(propertyId);

        assertThat(exists).isTrue();
    }

    @Test
    void resolves_the_property_s_dues_configuration() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        when(getPropertyUseCase.getProperty(new GetPropertyQuery(propertyIdValue)))
                .thenReturn(new PropertyView(propertyIdValue, "Residence", "1 rue Test", "Casablanca",
                        com.architek.oikos.property.domain.valueobject.DuesCalculationMode.SHARES, new BigDecimal("1000")));

        PropertyDuesConfigurationView view = newAdapter().getDuesConfiguration(propertyId);

        assertThat(view).isEqualTo(new PropertyDuesConfigurationView(DuesCalculationMode.SHARES, new BigDecimal("1000")));
    }

    @Test
    void resolves_each_unit_s_shares() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        BuildingId buildingA = BuildingId.newId();
        UnitId unitA1 = UnitId.newId();

        when(listBuildingsByPropertyUseCase.listBuildings(new ListBuildingsByPropertyQuery(propertyIdValue, PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(new BuildingView(buildingA, propertyIdValue, "A", 3)), 0, 100, 1));
        when(listUnitsByBuildingUseCase.listUnits(new ListUnitsByBuildingQuery(buildingA, PageRequest.of(0, 100), null)))
                .thenReturn(Page.of(List.of(
                        new UnitView(unitA1, buildingA, propertyIdValue, "A1", UnitTypeDefinitionId.newId(), "Appartement",
                                new BigDecimal("150"), OwnershipStatus.AFFECTED, List.of(), null)),
                        0, 100, 1));

        List<UnitShareLine> lines = newAdapter().listUnitShares(propertyId);

        assertThat(lines).containsExactly(new UnitShareLine(unitA1.value(), new BigDecimal("150")));
    }

    @Test
    void exists_returns_false_when_the_property_is_not_found() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        when(getPropertyUseCase.getProperty(new GetPropertyQuery(propertyIdValue)))
                .thenThrow(new PropertyNotFoundException(propertyIdValue));

        boolean exists = newAdapter().exists(propertyId);

        assertThat(exists).isFalse();
    }

    @Test
    void collects_unit_ids_of_every_building_of_the_property() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        BuildingId buildingA = BuildingId.newId();
        BuildingId buildingB = BuildingId.newId();
        UnitId unitA1 = UnitId.newId();
        UnitId unitB1 = UnitId.newId();

        when(listBuildingsByPropertyUseCase.listBuildings(new ListBuildingsByPropertyQuery(propertyIdValue,
                PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(
                        new BuildingView(buildingA, propertyIdValue, "A", 3),
                        new BuildingView(buildingB, propertyIdValue, "B", 2)), 0, 100, 2));

        when(listUnitsByBuildingUseCase.listUnits(new ListUnitsByBuildingQuery(buildingA,
                PageRequest.of(0, 100), null)))
                .thenReturn(Page.of(List.of(
                        new UnitView(unitA1, buildingA, propertyIdValue, "A1", UnitTypeDefinitionId.newId(), "Appartement", BigDecimal.TEN,
                                OwnershipStatus.AFFECTED, List.of(), null)), 0, 100, 1));

        when(listUnitsByBuildingUseCase.listUnits(new ListUnitsByBuildingQuery(buildingB,
                PageRequest.of(0, 100), null)))
                .thenReturn(Page.of(List.of(
                        new UnitView(unitB1, buildingB, propertyIdValue, "B1", UnitTypeDefinitionId.newId(), "Appartement", BigDecimal.TEN,
                                OwnershipStatus.AFFECTED, List.of(), null)), 0, 100, 1));

        List<EntityId> unitIds = newAdapter().listUnitIds(propertyId);

        assertThat(unitIds).containsExactlyInAnyOrder(unitA1.value(), unitB1.value());
    }

    @Test
    void maps_every_unit_of_the_property_to_its_lot_number() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        BuildingId buildingA = BuildingId.newId();
        UnitId unitA1 = UnitId.newId();

        when(listBuildingsByPropertyUseCase.listBuildings(new ListBuildingsByPropertyQuery(propertyIdValue,
                PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(new BuildingView(buildingA, propertyIdValue, "A", 3)), 0, 100, 1));

        when(listUnitsByBuildingUseCase.listUnits(new ListUnitsByBuildingQuery(buildingA,
                PageRequest.of(0, 100), "A1")))
                .thenReturn(Page.of(List.of(
                        new UnitView(unitA1, buildingA, propertyIdValue, "A1", UnitTypeDefinitionId.newId(), "Appartement", BigDecimal.TEN,
                                OwnershipStatus.AFFECTED, List.of(), null)), 0, 100, 1));

        Map<EntityId, String> unitNumberById = newAdapter().listUnitNumbersById(propertyId, "A1");

        assertThat(unitNumberById).containsExactly(entry(unitA1.value(), "A1"));
    }

    @Test
    void returns_an_empty_list_when_the_property_has_no_building() {
        EntityId propertyId = EntityId.newId();
        when(listBuildingsByPropertyUseCase.listBuildings(new ListBuildingsByPropertyQuery(new PropertyId(propertyId),
                PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(), 0, 100, 0));

        List<EntityId> unitIds = newAdapter().listUnitIds(propertyId);

        assertThat(unitIds).isEmpty();
    }

    @Test
    void resolves_each_unit_s_price_from_its_type_and_leaves_it_null_when_unpriced() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        BuildingId buildingA = BuildingId.newId();
        UnitId pricedUnit = UnitId.newId();
        UnitId unpricedUnit = UnitId.newId();
        UnitTypeDefinitionId pricedTypeId = UnitTypeDefinitionId.newId();
        UnitTypeDefinitionId unpricedTypeId = UnitTypeDefinitionId.newId();

        when(listBuildingsByPropertyUseCase.listBuildings(new ListBuildingsByPropertyQuery(propertyIdValue, PageRequest.of(0, 100))))
                .thenReturn(Page.of(List.of(new BuildingView(buildingA, propertyIdValue, "A", 3)), 0, 100, 1));
        when(listUnitsByBuildingUseCase.listUnits(new ListUnitsByBuildingQuery(buildingA, PageRequest.of(0, 100), null)))
                .thenReturn(Page.of(List.of(
                        new UnitView(pricedUnit, buildingA, propertyIdValue, "A1", pricedTypeId, "Appartement", BigDecimal.TEN, OwnershipStatus.AFFECTED, List.of(), null),
                        new UnitView(unpricedUnit, buildingA, propertyIdValue, "A2", unpricedTypeId, "Box", BigDecimal.TEN, OwnershipStatus.AFFECTED, List.of(), null)),
                        0, 100, 2));
        when(listUnitTypePricesByPropertyUseCase.listUnitTypePrices(new ListUnitTypePricesByPropertyQuery(propertyIdValue)))
                .thenReturn(List.of(new UnitTypePriceView(UnitTypePricingId.newId(), propertyIdValue, pricedTypeId,
                        "Appartement", Price.of(new BigDecimal("300")))));

        List<UnitPriceLine> lines = newAdapter().listUnitPrices(propertyId);

        assertThat(lines).containsExactlyInAnyOrder(
                new UnitPriceLine(pricedUnit.value(), new BigDecimal("300")),
                new UnitPriceLine(unpricedUnit.value(), null));
    }
}
