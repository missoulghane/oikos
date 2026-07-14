package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BuildingRepository;

@ExtendWith(MockitoExtension.class)
class CreatePropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BuildingRepository buildingRepository;

    private CreatePropertyService newService() {
        return new CreatePropertyService(propertyRepository, buildingRepository);
    }

    @Test
    void creating_a_property_also_persists_its_first_building() {
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePropertyCommand command = new CreatePropertyCommand("Copro Laumiere", "33 Avenue de Laumiere", "Batiment A", 5);

        newService().create(command);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().getName()).isEqualTo("Copro Laumiere");

        ArgumentCaptor<Building> buildingCaptor = ArgumentCaptor.forClass(Building.class);
        verify(buildingRepository).save(buildingCaptor.capture());
        assertThat(buildingCaptor.getValue().getName()).isEqualTo("Batiment A");
        assertThat(buildingCaptor.getValue().getPropertyId()).isEqualTo(propertyCaptor.getValue().getId());
    }
}
