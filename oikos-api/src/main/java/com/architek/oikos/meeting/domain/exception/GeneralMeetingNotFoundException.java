package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class GeneralMeetingNotFoundException extends ResourceNotFoundException {

    public GeneralMeetingNotFoundException(GeneralMeetingId id) {
        super("General meeting not found with id: " + id);
    }
}
