package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Thrown when a unit is claimed (via ClaimUnitOwnershipUseCase) but already
 * has an owner - either a genuine race lost against a concurrent claim, or a
 * stale invitation/membership request targeting a unit that was assigned in the
 * meantime. Deliberately not a BusinessException-default 400: this is a
 * state conflict, not an invalid input.
 */
public class UnitAlreadyClaimedException extends ConflictException {

    public UnitAlreadyClaimedException(UnitId unitId) {
        super("Unit " + unitId + " is already assigned to an owner");
    }
}
