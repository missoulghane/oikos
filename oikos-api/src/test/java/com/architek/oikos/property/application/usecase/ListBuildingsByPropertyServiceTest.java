package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListBuildingsByPropertyQuery;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListBuildingsByPropertyServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Test
    void listing_buildings_maps_the_repository_page_to_views() {
        PropertyId propertyId = PropertyId.newId();
        Building building = Building.create(BuildingId.newId(), propertyId, "Batiment A", 5);
        when(buildingRepository.findAllByPropertyId(propertyId, PageRequest.defaultRequest()))
                .thenReturn(Page.of(List.of(building), 0, 20, 1));

        var query = new ListBuildingsByPropertyQuery(propertyId, PageRequest.defaultRequest());
        var page = new ListBuildingsByPropertyService(buildingRepository).listBuildings(query);

        assertThat(page.content()).extracting(view -> view.name()).containsExactly("Batiment A");
    }
}
