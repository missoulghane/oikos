package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.exception.PartyAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ClaimUnitOwnershipServiceTest {

    @Mock
    private AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    private ClaimUnitOwnershipService newService() {
        return new ClaimUnitOwnershipService(addUnitOwnershipUseCase);
    }

    @Test
    void claiming_a_free_unit_requests_the_full_share_and_returns_the_ownership_id() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        UnitOwnershipId ownershipId = UnitOwnershipId.newId();
        when(addUnitOwnershipUseCase.add(any())).thenReturn(ownershipId);

        UnitOwnershipId result = newService().claim(new ClaimUnitOwnershipCommand(unitId, partyId));

        assertThat(result).isEqualTo(ownershipId);
        AddUnitOwnershipCommand expected = new AddUnitOwnershipCommand(unitId, partyId, new BigDecimal("100"));
        org.mockito.Mockito.verify(addUnitOwnershipUseCase).add(eq(expected));
    }

    @Test
    void claiming_a_unit_that_already_has_an_owner_is_reported_as_already_claimed() {
        when(addUnitOwnershipUseCase.add(any())).thenThrow(new OwnershipShareExceededException(UnitId.newId()));

        assertThatThrownBy(() -> newService().claim(new ClaimUnitOwnershipCommand(UnitId.newId(), EntityId.newId())))
                .isInstanceOf(UnitAlreadyClaimedException.class);
    }

    @Test
    void claiming_a_unit_the_party_already_owns_is_reported_as_already_claimed() {
        when(addUnitOwnershipUseCase.add(any())).thenThrow(new PartyAlreadyOwnsUnitException());

        assertThatThrownBy(() -> newService().claim(new ClaimUnitOwnershipCommand(UnitId.newId(), EntityId.newId())))
                .isInstanceOf(UnitAlreadyClaimedException.class);
    }
}
