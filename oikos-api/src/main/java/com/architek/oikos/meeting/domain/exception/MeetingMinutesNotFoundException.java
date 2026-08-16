package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class MeetingMinutesNotFoundException extends ResourceNotFoundException {

    public MeetingMinutesNotFoundException(GeneralMeetingId generalMeetingId) {
        super("No minutes drafted yet for general meeting: " + generalMeetingId);
    }
}
