package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.MyConvocationView;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

public record MyConvocationResponse(String id, String generalMeetingId, String propertyName, String meetingTitle,
                                     MeetingType meetingType, MeetingStatus meetingStatus, Instant scheduledAt,
                                     VenueType venueType, String venueAddress, String venueLink, String unitNumber,
                                     String buildingName, String meetingComment, AttendanceReply attendanceReply,
                                     boolean checkedIn) {

    public static MyConvocationResponse from(MyConvocationView view) {
        return new MyConvocationResponse(view.id().toString(), view.generalMeetingId().toString(), view.propertyName(),
                view.meetingTitle(), view.meetingType(), view.meetingStatus(), view.scheduledAt(), view.venueType(),
                view.venueAddress(), view.venueLink(), view.unitNumber(), view.buildingName(), view.meetingComment(),
                view.attendanceReply(), view.checkedIn());
    }
}
