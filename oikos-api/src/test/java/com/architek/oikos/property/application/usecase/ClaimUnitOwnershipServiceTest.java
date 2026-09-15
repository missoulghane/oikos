package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.exception.PartyAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ClaimUnitOwnershipServiceTest {

    @Mock
    private AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitOwnershipRepository unitOwnershipRepository;

    private ClaimUnitOwnershipService newService() {
        return new ClaimUnitOwnershipService(addUnitOwnershipUseCase, unitRepository, unitOwnershipRepository);
    }

    /** Le lot existe et se laisse verrouiller ; ce qu'il porte est dit par chaque test. */
    private void existingUnit(UnitId unitId, List<UnitOwnership> ownerships) {
        when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(Unit.create(unitId, BuildingId.newId(),
                PropertyId.newId(), "A12", UnitTypeDefinitionId.newId(), Shares.of(BigDecimal.TEN))));
        when(unitOwnershipRepository.findAllByUnitId(unitId)).thenReturn(ownerships);
    }

    @Test
    void claiming_a_free_unit_requests_the_full_share_and_returns_the_ownership_id() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        UnitOwnershipId ownershipId = UnitOwnershipId.newId();
        existingUnit(unitId, List.of());
        when(addUnitOwnershipUseCase.add(any())).thenReturn(ownershipId);

        UnitOwnershipId result = newService().claim(new ClaimUnitOwnershipCommand(unitId, partyId));

        assertThat(result).isEqualTo(ownershipId);
        AddUnitOwnershipCommand expected = new AddUnitOwnershipCommand(unitId, partyId, new BigDecimal("100"));
        org.mockito.Mockito.verify(addUnitOwnershipUseCase).add(eq(expected));
    }

    @Test
    void claiming_a_unit_that_already_has_an_owner_is_reported_as_already_claimed() {
        UnitId unitId = UnitId.newId();
        existingUnit(unitId, List.of(ownershipOf(unitId, EntityId.newId())));
        when(addUnitOwnershipUseCase.add(any())).thenThrow(new OwnershipShareExceededException(unitId));

        assertThatThrownBy(() -> newService().claim(new ClaimUnitOwnershipCommand(unitId, EntityId.newId())))
                .isInstanceOf(UnitAlreadyClaimedException.class);
    }

    /**
     * Le cas nominal d'une invitation privée, et celui qui faisait tout tomber :
     * l'attribution existe déjà, il n'y a rien à écrire. Laisser
     * AddUnitOwnershipService lever PartyAlreadyOwnsUnitException marquait la
     * transaction de l'appelant rollback-only, et son commit échouait en
     * UnexpectedRollbackException quel que soit le catch posé en amont.
     */
    @Test
    void claiming_a_unit_the_party_already_owns_returns_that_ownership_without_writing() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        UnitOwnership existing = ownershipOf(unitId, partyId);
        existingUnit(unitId, List.of(existing));

        UnitOwnershipId result = newService().claim(new ClaimUnitOwnershipCommand(unitId, partyId));

        assertThat(result).isEqualTo(existing.getId());
        org.mockito.Mockito.verify(addUnitOwnershipUseCase, org.mockito.Mockito.never()).add(any());
    }

    @Test
    void claiming_an_unknown_unit_is_rejected() {
        UnitId unitId = UnitId.newId();
        when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().claim(new ClaimUnitOwnershipCommand(unitId, EntityId.newId())))
                .isInstanceOf(UnitNotFoundException.class);
    }

    private static UnitOwnership ownershipOf(UnitId unitId, EntityId partyId) {
        return UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, PropertyId.newId(),
                OwnershipShare.of(new BigDecimal("100")));
    }
}
