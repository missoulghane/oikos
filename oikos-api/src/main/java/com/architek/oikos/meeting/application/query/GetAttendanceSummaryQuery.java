package com.architek.oikos.meeting.application.query;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public record GetAttendanceSummaryQuery(GeneralMeetingId generalMeetingId) {
}
