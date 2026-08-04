package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.query.ListAvailableUnitsByPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListAvailableUnitsByPropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    private ListAvailableUnitsByPropertyService newService() {
        return new ListAvailableUnitsByPropertyService(
                propertyRepository, buildingRepository, unitRepository, unitOwnershipRepository, unitTypeDefinitionRepository);
    }

    @Test
    void listing_available_units_of_a_missing_property_throws() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService()
                .listAvailableUnits(new ListAvailableUnitsByPropertyQuery(propertyId, PageRequest.of(0, 20))))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void only_units_without_any_owner_are_returned() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(
                Optional.of(Property.create(propertyId, "Copro Test", "1 rue de la Paix")));

        BuildingId buildingId = BuildingId.newId();
        Building building = Building.create(buildingId, propertyId, "Bâtiment A", 3);
        when(buildingRepository.findAllByPropertyId(propertyId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(building), 0, 100, 1));

        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        Unit freeUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A1", unitTypeId, Shares.of(BigDecimal.TEN));
        Unit takenUnit = Unit.create(UnitId.newId(), buildingId, propertyId, "A2", unitTypeId, Shares.of(BigDecimal.TEN));
        when(unitRepository.findAllByBuildingId(buildingId, PageRequest.of(0, 100)))
                .thenReturn(Page.of(List.of(freeUnit, takenUnit), 0, 100, 2));

        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId)).thenReturn(List.of());
        when(unitOwnershipRepository.findAllByUnitId(freeUnit.getId())).thenReturn(List.of());
        when(unitOwnershipRepository.findAllByUnitId(takenUnit.getId())).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), takenUnit.getId(), EntityId.newId(), propertyId,
                        OwnershipShare.of(new BigDecimal("100")))));

        Page<UnitView> page = newService()
                .listAvailableUnits(new ListAvailableUnitsByPropertyQuery(propertyId, PageRequest.of(0, 20)));

        assertThat(page.content()).extracting(UnitView::unitNumber).containsExactly("A1");
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
