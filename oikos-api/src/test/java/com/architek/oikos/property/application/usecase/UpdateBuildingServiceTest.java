package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.UpdateBuildingCommand;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;

@ExtendWith(MockitoExtension.class)
class UpdateBuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    private UpdateBuildingService newService() {
        return new UpdateBuildingService(buildingRepository);
    }

    @Test
    void renames_the_building_and_fixes_its_floor_count() {
        BuildingId id = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        when(buildingRepository.findById(id))
                .thenReturn(Optional.of(Building.create(id, propertyId, "Bâtiment A", 5)));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BuildingView view = newService().update(new UpdateBuildingCommand(id, "Bâtiment B", 7));

        assertThat(view.name()).isEqualTo("Bâtiment B");
        assertThat(view.floorCount()).isEqualTo(7);
    }

    @Test
    void the_building_stays_attached_to_its_property() {
        // Déplacer un bâtiment d'une copropriété à une autre emporterait ses lots,
        // leurs propriétaires et leurs appels de charges : ce n'est pas une
        // modification de fiche, et la commande ne le permet même pas.
        BuildingId id = BuildingId.newId();
        PropertyId propertyId = PropertyId.newId();
        when(buildingRepository.findById(id))
                .thenReturn(Optional.of(Building.create(id, propertyId, "Bâtiment A", 5)));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BuildingView view = newService().update(new UpdateBuildingCommand(id, "Bâtiment B", 7));

        assertThat(view.id()).isEqualTo(id);
        assertThat(view.propertyId()).isEqualTo(propertyId);
    }

    @Test
    void a_ground_floor_only_building_is_accepted() {
        // Zéro étage est une villa ou un local commercial, pas une saisie ratée.
        BuildingId id = BuildingId.newId();
        when(buildingRepository.findById(id))
                .thenReturn(Optional.of(Building.create(id, PropertyId.newId(), "Villa", 2)));
        when(buildingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(newService().update(new UpdateBuildingCommand(id, "Villa", 0)).floorCount()).isZero();
    }

    @Test
    void a_negative_floor_count_is_refused_by_the_domain() {
        BuildingId id = BuildingId.newId();
        when(buildingRepository.findById(id))
                .thenReturn(Optional.of(Building.create(id, PropertyId.newId(), "Bâtiment A", 5)));

        assertThatThrownBy(() -> newService().update(new UpdateBuildingCommand(id, "Bâtiment A", -1)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(buildingRepository, never()).save(any());
    }

    @Test
    void updating_a_missing_building_throws() {
        BuildingId id = BuildingId.newId();
        when(buildingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().update(new UpdateBuildingCommand(id, "Bâtiment B", 7)))
                .isInstanceOf(BuildingNotFoundException.class);
        verify(buildingRepository, never()).save(any());
    }
}
