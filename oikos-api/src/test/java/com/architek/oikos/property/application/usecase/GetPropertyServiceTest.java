package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;

@ExtendWith(MockitoExtension.class)
class GetPropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Test
    void getting_an_existing_property_returns_its_view() {
        PropertyId id = PropertyId.newId();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(Property.create(id, "Copro", "Address")));

        var view = new GetPropertyService(propertyRepository).getProperty(new GetPropertyQuery(id));

        assertThat(view.name()).isEqualTo("Copro");
    }

    @Test
    void getting_a_missing_property_throws() {
        PropertyId id = PropertyId.newId();
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetPropertyService(propertyRepository).getProperty(new GetPropertyQuery(id)))
                .isInstanceOf(PropertyNotFoundException.class);
    }
}
