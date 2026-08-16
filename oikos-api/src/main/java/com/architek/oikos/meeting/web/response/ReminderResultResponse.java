package com.architek.oikos.meeting.web.response;

/** How many lots a reminder actually went out to - the silent ones with no reachable owner are not counted. */
public record ReminderResultResponse(int remindedCount) {
}
