package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;

@ExtendWith(MockitoExtension.class)
class GetBuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Test
    void getting_an_existing_building_returns_its_view() {
        BuildingId id = BuildingId.newId();
        when(buildingRepository.findById(id)).thenReturn(Optional.of(Building.create(id, PropertyId.newId(), "Batiment A", 5)));

        var view = new GetBuildingService(buildingRepository).getBuilding(new GetBuildingQuery(id));

        assertThat(view.name()).isEqualTo("Batiment A");
    }

    @Test
    void getting_a_missing_building_throws() {
        BuildingId id = BuildingId.newId();
        when(buildingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetBuildingService(buildingRepository).getBuilding(new GetBuildingQuery(id)))
                .isInstanceOf(BuildingNotFoundException.class);
    }
}
