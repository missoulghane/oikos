package com.architek.oikos.meeting.application.command;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Note the absence of a ReplySource: it is deduced from requestedByUserId, and
 * a client that could state its own source could write "the owner confirmed
 * from the app" about an answer nobody gave.
 *
 * <p>medium is the opposite case, and the contrast is the point. By what means
 * the answer reached the office is something only the person taking the call
 * knows, so it IS sent by the client - and is therefore a declaration, kept in
 * its own field rather than promoted into the source. It is ignored unless the
 * deduced source turns out to be OTHER: an owner answering from their own space
 * did not reach anybody by telephone.
 *
 * <p>receivedAt is likewise declared and may predate the request - a letter
 * that arrived on Tuesday is keyed in on Thursday. Null means "now", which is
 * the ordinary case of a syndic recording a call as it ends.
 *
 * <p>attendanceMode and byProxy say what the answer ANNOUNCES: in the room or
 * remotely, in person or through a stand-in. Neither grants anything - presence
 * is the check-in (ADR 0002 §5) - and byProxy is not the mandate, which is
 * still out of scope (ADR 0002 §7). Both are dropped unless the answer is
 * ATTENDING.
 */
public record ReplyToConvocationCommand(ConvocationId convocationId, AttendanceReply attendanceReply,
                                         AttendanceMode attendanceMode, boolean byProxy, ReplyMediumCode medium,
                                         Instant receivedAt, String note, EntityId requestedByUserId) {
}
