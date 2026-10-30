package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.UpdateUnitCommand;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.domain.exception.UnitFloorOutOfBuildingRangeException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@ExtendWith(MockitoExtension.class)
class UpdateUnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    private final PropertyId propertyId = PropertyId.newId();
    private final BuildingId buildingId = BuildingId.newId();
    private final UnitTypeDefinitionId unitTypeId = UnitTypeDefinitionId.newId();
    private final Unit unit = Unit.create(UnitId.newId(), buildingId, propertyId, "N° 1", unitTypeId,
            Shares.of(new BigDecimal("100")), 1);

    private UpdateUnitService newService() {
        return new UpdateUnitService(unitRepository, buildingRepository, unitOwnershipRepository,
                unitTypeDefinitionRepository);
    }

    @Test
    void corrects_the_number_the_floor_and_the_shares_keeping_the_type() {
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Bâtiment A", 4)));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of());
        when(unitTypeDefinitionRepository.findById(unitTypeId)).thenReturn(Optional.empty());

        UnitView view = newService().update(new UpdateUnitCommand(unit.getId(), "A12", 4, new BigDecimal("150")));

        assertThat(view.unitNumber()).isEqualTo("A12");
        assertThat(view.floor()).isEqualTo(4);
        assertThat(view.shares()).isEqualByComparingTo("150");
        assertThat(view.unitTypeId()).isEqualTo(unitTypeId);
    }

    @Test
    void clearing_the_floor_needs_no_building_check() {
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(unitRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitOwnershipRepository.findAllByUnitId(unit.getId())).thenReturn(List.of());
        when(unitTypeDefinitionRepository.findById(unitTypeId)).thenReturn(Optional.empty());

        UnitView view = newService().update(new UpdateUnitCommand(unit.getId(), "N° 1", null, new BigDecimal("100")));

        assertThat(view.floor()).isNull();
        verify(buildingRepository, never()).findById(any());
    }

    @Test
    void a_floor_above_the_building_is_refused_before_anything_is_saved() {
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(buildingRepository.findById(buildingId))
                .thenReturn(Optional.of(Building.create(buildingId, propertyId, "Bâtiment A", 4)));

        assertThatThrownBy(() -> newService().update(
                new UpdateUnitCommand(unit.getId(), "N° 1", 5, new BigDecimal("100"))))
                .isInstanceOf(UnitFloorOutOfBuildingRangeException.class);
        verify(unitRepository, never()).save(any());
    }
}
