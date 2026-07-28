package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListBuildingsByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListUnitTypePricesByPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListUnitsByBuildingUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;
import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: resolves everything installment needs about
 * properties by delegating to property's public port-in use cases - existence
 * checks (GetPropertyUseCase), every unit of a property (buildings, then
 * units per building), and unit type prices - never through property's
 * repositories directly (rule 4). Named distinctly from property's own
 * PropertyPartyDirectoryAdapter to avoid a Spring bean name collision between
 * same-named classes in different packages.
 */
@Component
public class InstallmentPropertyDirectoryAdapter implements PropertyUnitDirectoryPort, PropertyDirectoryPort, PropertyUnitPricingPort {

    private static final int PAGE_SIZE = 100;

    private final ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase;
    private final ListUnitsByBuildingUseCase listUnitsByBuildingUseCase;
    private final GetPropertyUseCase getPropertyUseCase;
    private final ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase;

    public InstallmentPropertyDirectoryAdapter(ListBuildingsByPropertyUseCase listBuildingsByPropertyUseCase,
                                            ListUnitsByBuildingUseCase listUnitsByBuildingUseCase,
                                            GetPropertyUseCase getPropertyUseCase,
                                            ListUnitTypePricesByPropertyUseCase listUnitTypePricesByPropertyUseCase) {
        this.listBuildingsByPropertyUseCase = listBuildingsByPropertyUseCase;
        this.listUnitsByBuildingUseCase = listUnitsByBuildingUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
        this.listUnitTypePricesByPropertyUseCase = listUnitTypePricesByPropertyUseCase;
    }

    @Override
    public boolean exists(EntityId propertyId) {
        try {
            getPropertyUseCase.getProperty(new GetPropertyQuery(new PropertyId(propertyId)));
            return true;
        } catch (PropertyNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<EntityId> listUnitIds(EntityId propertyId) {
        List<EntityId> unitIds = new ArrayList<>();
        for (BuildingView building : listAllBuildings(new PropertyId(propertyId))) {
            for (UnitView unit : listAllUnits(building.id())) {
                unitIds.add(unit.id().value());
            }
        }
        return unitIds;
    }

    @Override
    public List<UnitPriceLine> listUnitPrices(EntityId propertyId) {
        PropertyId typedPropertyId = new PropertyId(propertyId);
        Map<UnitTypeDefinitionId, BigDecimal> priceByUnitTypeId = listUnitTypePricesByPropertyUseCase
                .listUnitTypePrices(new ListUnitTypePricesByPropertyQuery(typedPropertyId)).stream()
                .collect(Collectors.toMap(UnitTypePriceView::unitTypeId, view -> view.price().value()));

        List<UnitPriceLine> lines = new ArrayList<>();
        for (BuildingView building : listAllBuildings(typedPropertyId)) {
            for (UnitView unit : listAllUnits(building.id())) {
                lines.add(new UnitPriceLine(unit.id().value(), priceByUnitTypeId.get(unit.unitTypeId())));
            }
        }
        return lines;
    }

    private List<BuildingView> listAllBuildings(PropertyId propertyId) {
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

    private List<UnitView> listAllUnits(BuildingId buildingId) {
        List<UnitView> units = new ArrayList<>();
        int pageNumber = 0;
        Page<UnitView> page;
        do {
            page = listUnitsByBuildingUseCase.listUnits(
                    new ListUnitsByBuildingQuery(buildingId, PageRequest.of(pageNumber, PAGE_SIZE)));
            units.addAll(page.content());
            pageNumber++;
        } while (page.hasNext());
        return units;
    }
}
