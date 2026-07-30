package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.port.out.UnitAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.PropertyConfigurationLimitExceededException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;

@ExtendWith(MockitoExtension.class)
class ConfigurePropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private UnitAccountProvisioningPort unitAccountProvisioningPort;

    private ConfigurePropertyService newService(int maxUnitsPerRequest) {
        return new ConfigurePropertyService(propertyRepository, buildingRepository, unitRepository,
                unitTypeDefinitionRepository, unitAccountProvisioningPort, maxUnitsPerRequest);
    }

    @Test
    void configuring_a_property_persists_its_buildings_and_generates_numbered_units_with_zero_shares() {
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurePropertyCommand command = new ConfigurePropertyCommand("My Property", "123 Main St", List.of(
                new BuildingConfiguration("Building A", 5, List.of(
                        new UnitTypeConfiguration("Appartement", 3),
                        new UnitTypeConfiguration("Box", 2)))));

        newService(500).configure(command);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().getName()).isEqualTo("My Property");

        ArgumentCaptor<Building> buildingCaptor = ArgumentCaptor.forClass(Building.class);
        verify(buildingRepository).save(buildingCaptor.capture());
        assertThat(buildingCaptor.getValue().getName()).isEqualTo("Building A");
        assertThat(buildingCaptor.getValue().getPropertyId()).isEqualTo(propertyCaptor.getValue().getId());

        ArgumentCaptor<Unit> unitCaptor = ArgumentCaptor.forClass(Unit.class);
        verify(unitRepository, org.mockito.Mockito.times(5)).save(unitCaptor.capture());
        List<Unit> units = unitCaptor.getAllValues();
        assertThat(units).extracting(Unit::getUnitNumber)
                .containsExactly("Appartement 1", "Appartement 2", "Appartement 3", "Box 1", "Box 2");
        assertThat(units).allMatch(unit -> unit.getBuildingId().equals(buildingCaptor.getValue().getId()));
        assertThat(units).allMatch(unit -> unit.getShares().value().compareTo(BigDecimal.ZERO) == 0);

        verify(unitAccountProvisioningPort, org.mockito.Mockito.times(5)).provisionAccount(any(), any());
    }

    @Test
    void configuring_a_property_always_seeds_a_default_others_unit_type() {
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurePropertyCommand command = new ConfigurePropertyCommand("My Property", "123 Main St", List.of());

        newService(500).configure(command);

        verify(unitTypeDefinitionRepository).save(argThat(unitType -> unitType.getName().equals("OTHERS")));
    }

    @Test
    void configuring_a_property_beyond_the_max_units_limit_is_rejected_without_persisting_anything() {
        ConfigurePropertyCommand command = new ConfigurePropertyCommand("My Property", "123 Main St", List.of(
                new BuildingConfiguration("Building A", 5, List.of(
                        new UnitTypeConfiguration("Appartement", 10)))));

        assertThatThrownBy(() -> newService(5).configure(command))
                .isInstanceOf(PropertyConfigurationLimitExceededException.class);

        verify(propertyRepository, never()).save(any());
        verify(buildingRepository, never()).save(any());
        verify(unitRepository, never()).save(any());
    }
}
