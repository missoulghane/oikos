package com.architek.oikos.meeting.web.request;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;

/**
 * No replySource: it is deduced from the authenticated caller. Letting a client
 * state how its own answer was obtained would let anyone write "the
 * copropriétaire confirmed from the app" about an answer nobody gave.
 *
 * <p>medium is the deliberate counterpart, and the two must not be confused.
 * How the answer reached the office - by telephone, by post - is something only
 * the person who took it knows, so it is sent by the client and kept as a
 * declaration rather than promoted into the source. It is ignored unless the
 * deduced source is OTHER.
 *
 * <p>receivedAt is when the answer was given, not when this request is made: a
 * letter that arrived on Tuesday is keyed in on Thursday. Null means now.
 *
 * <p>attendanceMode and byProxy describe what the answer announces - in the
 * room or remotely, in person or through a stand-in. The five choices the
 * screen offers are these three fields combined, not five statuses: "absent,
 * sur place" is not a state of the world, and the server refuses it.
 *
 * <p>note is the syndic's margin, for an answer given at the office: "called on
 * Tuesday", "told the caretaker".
 */
public record ReplyToConvocationRequest(@NotNull AttendanceReply attendanceReply, AttendanceMode attendanceMode,
                                         boolean byProxy, @Size(max = 30) String medium, Instant receivedAt,
                                         @Size(max = 500) String note) {
}
