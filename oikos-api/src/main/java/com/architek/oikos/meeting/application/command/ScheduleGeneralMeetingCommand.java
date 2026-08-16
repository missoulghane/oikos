package com.architek.oikos.meeting.application.command;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;

public record ScheduleGeneralMeetingCommand(GeneralMeetingId id, Instant scheduledAt, MeetingVenue venue) {
}
