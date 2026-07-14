package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddBuildingCommand;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;

@ExtendWith(MockitoExtension.class)
class AddBuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    private AddBuildingService newService() {
        return new AddBuildingService(buildingRepository, propertyRepository);
    }

    @Test
    void adding_an_building_to_an_existing_property_persists_it() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBuildingCommand(propertyId, "Batiment B", 3));
    }

    @Test
    void adding_an_building_to_an_unknown_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(new AddBuildingCommand(propertyId, "Batiment B", 3)))
                .isInstanceOf(PropertyNotFoundException.class);
    }
}
