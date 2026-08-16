package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * Carries no identifier of any kind - not the convocation's, not the lot's,
 * not the meeting's. The page it feeds is anonymous and the token is the only
 * handle it needs; an id here would only be an extra thing to leak.
 */
public record ConvocationConfirmationResponse(String propertyName, String meetingTitle, String meetingType,
                                               Instant scheduledAt, VenueType venueType, String venueAddress,
                                               String venueLink, String unitNumber, String buildingName,
                                               AttendanceReply attendanceReply, Instant repliedAt, boolean stillOpen) {

    public static ConvocationConfirmationResponse from(ConvocationConfirmationView view) {
        return new ConvocationConfirmationResponse(view.propertyName(), view.meetingTitle(), view.meetingType(),
                view.scheduledAt(), view.venueType(), view.venueAddress(), view.venueLink(), view.unitNumber(),
                view.buildingName(), view.attendanceReply(), view.repliedAt(), view.stillOpen());
    }
}
