package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListUnitTypeDefinitionsByPropertyQuery;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@ExtendWith(MockitoExtension.class)
class ListUnitTypeDefinitionsByPropertyServiceTest {

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    @Test
    void listing_unit_types_maps_the_repository_entries_to_views() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinition unitType = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement");
        when(unitTypeDefinitionRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(unitType));

        var views = new ListUnitTypeDefinitionsByPropertyService(unitTypeDefinitionRepository)
                .listUnitTypeDefinitions(new ListUnitTypeDefinitionsByPropertyQuery(propertyId));

        assertThat(views).extracting(view -> view.name()).containsExactly("Appartement");
    }
}
