package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;

/** checkedInPartyId is optional: whoever ticks off a room does not always know which co-owner came. */
public record CheckInConvocationRequest(@NotNull AttendanceMode attendanceMode, String checkedInPartyId) {
}
