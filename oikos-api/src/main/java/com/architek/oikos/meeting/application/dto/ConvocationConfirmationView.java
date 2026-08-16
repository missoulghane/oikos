package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * What the confirmation page shows to someone holding the link, and nothing
 * more.
 *
 * <p>Deliberately narrow. Whoever presents the token is entitled to see the
 * meeting they are convoked to and the lot it concerns - that is what the
 * letter already told them. They are not entitled to the other lots, to the
 * owners' names or addresses, to the attendance of the copropriété, or to
 * anything a syndic sees: the page is anonymous, and a token that leaked would
 * leak exactly this much.
 *
 * <p>stillOpen is what the page keys off: past the opening of the session, the
 * link shows the meeting and refuses to record anything.
 */
public record ConvocationConfirmationView(String propertyName, String meetingTitle, String meetingType,
                                           Instant scheduledAt, VenueType venueType, String venueAddress,
                                           String venueLink, String unitNumber, String buildingName,
                                           AttendanceReply attendanceReply, Instant repliedAt, boolean stillOpen) {
}
