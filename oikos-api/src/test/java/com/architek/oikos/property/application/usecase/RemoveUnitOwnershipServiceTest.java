package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.RemoveUnitOwnershipCommand;
import com.architek.oikos.property.domain.exception.UnitOwnershipNotFoundException;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RemoveUnitOwnershipServiceTest {

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    @Test
    void removing_an_existing_entry_deletes_it() {
        UnitOwnershipId id = UnitOwnershipId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(id, UnitId.newId(), EntityId.newId(), PropertyId.newId(),
                OwnershipShare.of(BigDecimal.TEN));
        when(unitOwnershipRepository.findById(id)).thenReturn(Optional.of(unitOwnership));

        new RemoveUnitOwnershipService(unitOwnershipRepository).remove(new RemoveUnitOwnershipCommand(id));

        verify(unitOwnershipRepository).deleteById(id);
    }

    @Test
    void removing_an_unknown_entry_throws() {
        UnitOwnershipId id = UnitOwnershipId.newId();
        when(unitOwnershipRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RemoveUnitOwnershipService(unitOwnershipRepository).remove(new RemoveUnitOwnershipCommand(id)))
                .isInstanceOf(UnitOwnershipNotFoundException.class);
    }
}
