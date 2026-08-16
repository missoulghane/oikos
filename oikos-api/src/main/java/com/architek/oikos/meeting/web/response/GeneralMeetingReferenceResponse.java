package com.architek.oikos.meeting.web.response;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public record GeneralMeetingReferenceResponse(String id) {

    public static GeneralMeetingReferenceResponse from(GeneralMeetingId id) {
        return new GeneralMeetingReferenceResponse(id.toString());
    }
}
