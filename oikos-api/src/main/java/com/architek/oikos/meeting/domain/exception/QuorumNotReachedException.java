package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Opening a session without quorum is not forbidden - it is a decision with
 * legal consequences that the syndic must take deliberately. Hence a refusal
 * by default with an explicit override (see GeneralMeeting.open), rather than
 * a warning nobody reads.
 */
public class QuorumNotReachedException extends ConflictException {

    public QuorumNotReachedException() {
        super("The quorum is not reached - open the session explicitly with forceWithoutQuorum to proceed anyway");
    }
}
