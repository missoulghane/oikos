package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.exception.UnitFloorOutOfBuildingRangeException;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@ExtendWith(MockitoExtension.class)
class AddUnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private LedgerAccountProvisioningPort ledgerAccountProvisioningPort;

    private AddUnitService newService() {
        return new AddUnitService(unitRepository, buildingRepository, unitTypeDefinitionRepository,
                ledgerAccountProvisioningPort);
    }

    @Test
    void adding_a_unit_to_an_existing_building_persists_it() {
        PropertyId propertyId = PropertyId.newId();
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, new BigDecimal("150"), 2));

        verify(unitRepository).save(any());
        verify(ledgerAccountProvisioningPort).provisionUnitReceivableAccount(any(), any());
    }

    @Test
    void adding_a_unit_to_an_unknown_building_is_rejected() {
        BuildingId buildingId = BuildingId.newId();
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(
                new AddUnitCommand(buildingId, "A12", UnitTypeDefinitionId.newId(), BigDecimal.TEN, null)))
                .isInstanceOf(BuildingNotFoundException.class);
    }

    @Test
    void adding_a_unit_with_a_unit_type_from_another_property_is_rejected() {
        BuildingId buildingId = BuildingId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, PropertyId.newId(), "Batiment A", 5)));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, PropertyId.newId(), "Appartement")));

        assertThatThrownBy(() -> newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, BigDecimal.TEN, null)))
                .isInstanceOf(UnitTypeDefinitionNotFoundException.class);
    }

    @Test
    void adding_a_unit_on_a_floor_the_building_does_not_have_is_rejected() {
        BuildingId buildingId = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 3)));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));

        assertThatThrownBy(() -> newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, BigDecimal.TEN, 4)))
                .isInstanceOf(UnitFloorOutOfBuildingRangeException.class);
    }

    @Test
    void the_top_floor_of_the_building_is_allowed_since_zero_is_the_ground_floor() {
        BuildingId buildingId = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 3)));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, BigDecimal.TEN, 3));

        verify(unitRepository).save(any());
    }

    @Test
    void a_unit_may_be_added_without_saying_which_floor_it_is_on() {
        BuildingId buildingId = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Batiment A", 3)));
        when(unitTypeDefinitionRepository.findById(unitTypeId))
                .thenReturn(Optional.of(UnitTypeDefinition.create(unitTypeId, propertyId, "Appartement")));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, BigDecimal.TEN, null));

        verify(unitRepository).save(any());
    }
}
