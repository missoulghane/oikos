package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;

/** The whole body: whoever holds the link says whether the lot will be there. */
public record ConfirmConvocationRequest(@NotNull AttendanceReply attendanceReply) {
}
