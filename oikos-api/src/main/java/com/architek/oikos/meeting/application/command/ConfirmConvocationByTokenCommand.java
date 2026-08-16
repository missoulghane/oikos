package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;

/**
 * No user id and no reply source: the caller is anonymous by construction, and
 * the source is necessarily OWNER_LINK - which is precisely what this entry
 * point is for.
 */
public record ConfirmConvocationByTokenCommand(String token, AttendanceReply attendanceReply) {
}
