package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Drafting the minutes of a session still under way. The minutes freeze the
 * attendance and every ballot; produced mid-session they would record a state
 * that is still moving, and nothing would ever refresh them.
 */
public class MeetingNotClosedException extends ConflictException {

    public MeetingNotClosedException(MeetingStatus status) {
        super("The minutes can only be drafted once the session is closed, current status: " + status);
    }
}
