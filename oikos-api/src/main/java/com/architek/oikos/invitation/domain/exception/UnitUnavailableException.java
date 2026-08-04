package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Thrown when claiming a unit for an invitation fails because it already has
 * an owner - either a genuine race lost against a concurrent claim, or a
 * stale invitation targeting a unit that was assigned through some other
 * channel since it was issued.
 */
public class UnitUnavailableException extends ConflictException {

    public UnitUnavailableException(String message) {
        super(message);
    }
}
