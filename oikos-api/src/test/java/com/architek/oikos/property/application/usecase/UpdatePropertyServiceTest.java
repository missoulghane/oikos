package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;

@ExtendWith(MockitoExtension.class)
class UpdatePropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    private UpdatePropertyService newService() {
        return new UpdatePropertyService(propertyRepository);
    }

    @Test
    void updating_a_property_persists_the_new_name_and_address() {
        PropertyId id = PropertyId.newId();
        Property property = Property.create(id, "Copro Laumiere", "33 Avenue de Laumiere");
        when(propertyRepository.findById(id)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var command = new UpdatePropertyCommand(id, "Copro Renamed", "New address", "Rabat");
        var view = newService().update(command);

        assertThat(view.name()).isEqualTo("Copro Renamed");
        assertThat(view.address()).isEqualTo("New address");
    }

    @Test
    void updating_a_missing_property_throws() {
        PropertyId id = PropertyId.newId();
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        var command = new UpdatePropertyCommand(id, "Copro Renamed", "New address", "Rabat");

        assertThatThrownBy(() -> newService().update(command)).isInstanceOf(PropertyNotFoundException.class);
    }
}
