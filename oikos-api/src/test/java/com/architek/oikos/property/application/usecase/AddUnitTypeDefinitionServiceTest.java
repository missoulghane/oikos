package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitTypeDefinitionCommand;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.exception.UnitTypeNameAlreadyUsedException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;

@ExtendWith(MockitoExtension.class)
class AddUnitTypeDefinitionServiceTest {

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Mock
    private PropertyRepository propertyRepository;

    private AddUnitTypeDefinitionService newService() {
        return new AddUnitTypeDefinitionService(unitTypeDefinitionRepository, propertyRepository);
    }

    @Test
    void adding_a_unit_type_to_an_existing_property_persists_it() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(unitTypeDefinitionRepository.existsByPropertyIdAndName(propertyId, "Duplex")).thenReturn(false);
        when(unitTypeDefinitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddUnitTypeDefinitionCommand(propertyId, "Duplex"));
    }

    @Test
    void adding_a_unit_type_with_a_name_already_used_on_the_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(unitTypeDefinitionRepository.existsByPropertyIdAndName(propertyId, "Appartement")).thenReturn(true);

        assertThatThrownBy(() -> newService().add(new AddUnitTypeDefinitionCommand(propertyId, "Appartement")))
                .isInstanceOf(UnitTypeNameAlreadyUsedException.class);
    }

    @Test
    void adding_a_unit_type_to_an_unknown_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(new AddUnitTypeDefinitionCommand(propertyId, "Duplex")))
                .isInstanceOf(PropertyNotFoundException.class);
    }
}
