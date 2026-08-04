package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

/**
 * Attaches a party to a unit it must be the SOLE owner of (e.g. an invitation
 * being consumed) - unlike AddUnitOwnershipUseCase, which allows any share
 * and any number of co-owners. Used wherever "claim this whole unit,
 * atomically, or fail if someone got there first" is the required semantics.
 */
public interface ClaimUnitOwnershipUseCase {

    UnitOwnershipId claim(ClaimUnitOwnershipCommand command);
}
