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

import com.architek.oikos.property.application.query.ListUnitsByBuildingQuery;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

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

    @Test
    void listing_units_maps_the_repository_page_to_views_with_the_occupation_status() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit unit = Unit.create(UnitId.newId(), buildingId, "A12", unitTypeId, Shares.of(new BigDecimal("150")));
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId))
                .thenReturn(List.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.defaultRequest()))
                .thenReturn(Page.of(List.of(unit), 0, 20, 1));
        when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of());

        var query = new ListUnitsByBuildingQuery(buildingId, PageRequest.defaultRequest());
        var page = new ListUnitsByBuildingService(unitRepository, unitOwnershipRepository, unitTypeDefinitionRepository,
                buildingRepository).listUnits(query);

        assertThat(page.content()).extracting(view -> view.unitNumber()).containsExactly("A12");
        assertThat(page.content()).extracting(view -> view.unitTypeName()).containsExactly("Appartement");
        assertThat(page.content()).extracting(view -> view.ownershipStatus())
                .containsExactly(OwnershipStatus.UNSOLD_DEVELOPER);
    }
}
