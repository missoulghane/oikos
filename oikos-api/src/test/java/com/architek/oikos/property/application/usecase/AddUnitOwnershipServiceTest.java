package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.domain.exception.ContactAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AddUnitOwnershipServiceTest {

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Mock
    private UnitRepository unitRepository;

    private AddUnitOwnershipService newService() {
        return new AddUnitOwnershipService(unitOwnershipRepository, unitRepository);
    }

    private static Unit existingUnit(UnitId id) {
        return Unit.create(id, BuildingId.newId(), "A12", UnitType.APARTMENT, Shares.of(BigDecimal.TEN));
    }

    @Test
    void adding_an_owner_within_the_available_share_persists_it() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(unitOwnershipRepository.existsByUnitIdAndContactId(any(), any())).thenReturn(false);
        when(unitOwnershipRepository.findAllByUnitId(unitId)).thenReturn(List.of());
        when(unitOwnershipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddUnitOwnershipCommand(unitId, EntityId.newId(), new BigDecimal("60")));
    }

    @Test
    void adding_an_owner_that_would_push_the_total_share_above_100_is_rejected() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(unitOwnershipRepository.existsByUnitIdAndContactId(any(), any())).thenReturn(false);
        when(unitOwnershipRepository.findAllByUnitId(unitId)).thenReturn(List.of(
                UnitOwnership.create(UnitOwnershipId.newId(), unitId, EntityId.newId(), OwnershipShare.of(new BigDecimal("60")))));

        assertThatThrownBy(() -> newService().add(new AddUnitOwnershipCommand(unitId, EntityId.newId(), new BigDecimal("41"))))
                .isInstanceOf(OwnershipShareExceededException.class);
    }

    @Test
    void adding_the_same_contact_twice_on_the_same_unit_is_rejected() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId)));
        when(unitOwnershipRepository.existsByUnitIdAndContactId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> newService().add(new AddUnitOwnershipCommand(unitId, EntityId.newId(), BigDecimal.TEN)))
                .isInstanceOf(ContactAlreadyOwnsUnitException.class);
    }

    @Test
    void adding_an_owner_to_an_unknown_unit_is_rejected() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(new AddUnitOwnershipCommand(unitId, EntityId.newId(), BigDecimal.TEN)))
                .isInstanceOf(UnitNotFoundException.class);
    }
}
