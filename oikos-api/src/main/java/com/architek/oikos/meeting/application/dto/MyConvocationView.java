package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * The owner's own view of a meeting they are convoked to, joined to the
 * meeting itself so their screen needs a single call: what the meeting is,
 * which of their lots is concerned, and what they answered.
 *
 * <p>meetingComment is the syndic's note of intent, in rich-text HTML. It is
 * shown to the copropriétaire, which is the whole reason it exists - and it is
 * user input, so whatever displays it sanitizes it first.
 */
public record MyConvocationView(ConvocationId id, GeneralMeetingId generalMeetingId, String propertyName,
                                 String meetingTitle, MeetingType meetingType, MeetingStatus meetingStatus,
                                 Instant scheduledAt, VenueType venueType, String venueAddress, String venueLink,
                                 String unitNumber, String buildingName, String meetingComment,
                                 AttendanceReply attendanceReply, boolean checkedIn) {
}
