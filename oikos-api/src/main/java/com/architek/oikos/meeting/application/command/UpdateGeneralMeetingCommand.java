package com.architek.oikos.meeting.application.command;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;

/** scheduledAt and venue may still be null here: a draft is allowed to be incomplete. */
public record UpdateGeneralMeetingCommand(GeneralMeetingId id, MeetingType meetingType, String title,
                                           Instant scheduledAt, MeetingVenue venue) {
}
