package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public record GenerateMeetingMinutesCommand(GeneralMeetingId generalMeetingId) {
}
