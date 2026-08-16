package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * A lot voting without having signed in. The SFD makes presence the condition
 * of the vote, and it is the same fact the quorum is computed from - letting a
 * lot vote without it would put weight in the result that was never counted
 * towards opening the session.
 */
public class UnitNotCheckedInException extends BusinessException {

    public UnitNotCheckedInException(String unitLabel) {
        super("Lot " + unitLabel + " has not been checked in - only lots present at the session may vote");
    }
}
