package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * A vote arriving before the chair opened the ballot, or after it closed. 409
 * and not 400: the request was legal a moment earlier, or will be shortly -
 * the item moved, not the payload.
 */
public class VoteSessionNotOpenException extends ConflictException {

    public VoteSessionNotOpenException(VoteSessionStatus status) {
        super("Votes are only accepted while the ballot is open, current vote session status: " + status);
    }
}
