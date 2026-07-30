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
import com.architek.oikos.property.application.port.out.UnitAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
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
    private UnitAccountProvisioningPort unitAccountProvisioningPort;

    private AddUnitService newService() {
        return new AddUnitService(unitRepository, buildingRepository, unitTypeDefinitionRepository,
                unitAccountProvisioningPort);
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

        newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, new BigDecimal("150")));

        verify(unitAccountProvisioningPort).provisionAccount(any(), any());
    }

    @Test
    void adding_a_unit_to_an_unknown_building_is_rejected() {
        BuildingId buildingId = BuildingId.newId();
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(
                new AddUnitCommand(buildingId, "A12", UnitTypeDefinitionId.newId(), BigDecimal.TEN)))
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

        assertThatThrownBy(() -> newService().add(new AddUnitCommand(buildingId, "A12", unitTypeId, BigDecimal.TEN)))
                .isInstanceOf(UnitTypeDefinitionNotFoundException.class);
    }
}
