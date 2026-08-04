package com.architek.oikos.property.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.ClaimUnitOwnershipUseCase;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.exception.PartyAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

/**
 * Thin wrapper over AddUnitOwnershipUseCase requesting exactly 100% - which
 * already fails (OwnershipShareExceededException) whenever any prior owner
 * exists, even with a small share, so "the unit must be entirely free" falls
 * out of the existing invariant for free. Runs under AddUnitOwnershipService's
 * own pessimistic lock (see UnitRepository#findByIdForUpdate) - no separate
 * locking needed here.
 */
@Component
public class ClaimUnitOwnershipService implements ClaimUnitOwnershipUseCase {

    private static final BigDecimal FULL_SHARE = new BigDecimal("100");

    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    public ClaimUnitOwnershipService(AddUnitOwnershipUseCase addUnitOwnershipUseCase) {
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
    }

    @Override
    public UnitOwnershipId claim(ClaimUnitOwnershipCommand command) {
        try {
            return addUnitOwnershipUseCase.add(new AddUnitOwnershipCommand(command.unitId(), command.partyId(), FULL_SHARE));
        } catch (OwnershipShareExceededException | PartyAlreadyOwnsUnitException e) {
            throw new UnitAlreadyClaimedException(command.unitId());
        }
    }
}
