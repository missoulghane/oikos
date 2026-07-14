package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListPropertiesServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Test
    void listing_properties_maps_the_repository_page_to_views() {
        Property property = Property.create(PropertyId.newId(), "Copro", "Address");
        when(propertyRepository.findAll(PageRequest.defaultRequest())).thenReturn(Page.of(List.of(property), 0, 20, 1));

        var page = new ListPropertiesService(propertyRepository)
                .listProperties(new ListPropertiesQuery(PageRequest.defaultRequest()));

        assertThat(page.content()).extracting(view -> view.name()).containsExactly("Copro");
    }
}
