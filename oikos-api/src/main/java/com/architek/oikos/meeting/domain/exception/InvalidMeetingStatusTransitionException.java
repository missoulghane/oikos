package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * A 409 rather than a 400 (hence ConflictException and not BusinessException):
 * the request was well-formed and would have been legal a moment earlier - the
 * meeting has since moved on. Convoking twice from two open tabs is the usual
 * way to get here.
 */
public class InvalidMeetingStatusTransitionException extends ConflictException {

    public InvalidMeetingStatusTransitionException(MeetingStatus from, MeetingStatus to) {
        super("A general meeting cannot go from " + from + " to " + to);
    }
}
