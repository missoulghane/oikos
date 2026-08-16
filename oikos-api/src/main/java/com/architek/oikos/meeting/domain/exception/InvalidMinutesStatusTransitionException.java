package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;
import com.architek.oikos.shared.exception.ConflictException;

public class InvalidMinutesStatusTransitionException extends ConflictException {

    public InvalidMinutesStatusTransitionException(MinutesStatus from, MinutesStatus to) {
        super("Minutes cannot go from " + from + " to " + to);
    }
}
