package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Note the absence of a ReplySource: it is deduced from requestedByUserId, and
 * a client that could state its own source could write "the owner confirmed
 * from the app" about an answer nobody gave.
 */
public record ReplyToConvocationCommand(ConvocationId convocationId, AttendanceReply attendanceReply, String note,
                                         EntityId requestedByUserId) {
}
