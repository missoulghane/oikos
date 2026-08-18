package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;

/**
 * No user id and no reply source: the caller is anonymous by construction, and
 * the source is necessarily OWNER_LINK - which is precisely what this entry
 * point is for.
 *
 * <p>The lot's code travels with the answer even though the token alone would
 * find the convocation. The token proves the convocation was received; a link
 * is forwarded, printed and left on a table, and answering for someone else's
 * lot must not be one click away from whoever picks it up (ADR 0002 §16). The
 * caller id is the same anonymous IP as the paper path, and for the same
 * reason: it feeds the attempt cap.
 */
public record ConfirmConvocationByTokenCommand(String token, ShortCode confirmationCode,
                                                AttendanceReply attendanceReply, String callerId) {
}
