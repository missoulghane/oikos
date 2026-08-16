package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Voting, or opening a ballot, outside an open session. Guarded separately
 * from the ballot's own status because they fail for different reasons and a
 * syndic needs to know which: the meeting has not started (or is over), as
 * opposed to this particular point not being under discussion.
 */
public class MeetingNotInProgressException extends ConflictException {

    public MeetingNotInProgressException(MeetingStatus status) {
        super("The session must be in progress to run a ballot, current status: " + status);
    }
}
