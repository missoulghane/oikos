package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;

/**
 * No replySource: it is deduced from the authenticated caller. Letting a client
 * state how its own answer was obtained would let anyone write "the
 * copropriétaire confirmed from the app" about an answer nobody gave.
 *
 * <p>note is the syndic's margin, for an answer given at the office: "called on
 * Tuesday", "told the caretaker".
 */
public record ReplyToConvocationRequest(@NotNull AttendanceReply attendanceReply, @Size(max = 500) String note) {
}
