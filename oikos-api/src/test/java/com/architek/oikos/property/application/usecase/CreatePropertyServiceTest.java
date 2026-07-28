package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;

@ExtendWith(MockitoExtension.class)
class CreatePropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    private CreatePropertyService newService() {
        return new CreatePropertyService(propertyRepository, unitTypeDefinitionRepository);
    }

    @Test
    void creating_a_property_persists_it() {
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePropertyCommand command = new CreatePropertyCommand("Copro Laumiere", "33 Avenue de Laumiere");

        newService().create(command);

        ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().getName()).isEqualTo("Copro Laumiere");
    }

    @Test
    void creating_a_property_always_seeds_a_default_others_unit_type() {
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePropertyCommand command = new CreatePropertyCommand("Copro Laumiere", "33 Avenue de Laumiere");

        newService().create(command);

        verify(unitTypeDefinitionRepository).save(argThat(unitType -> unitType.getName().equals("OTHERS")));
    }
}
