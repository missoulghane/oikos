package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;

public record ConfirmConvocationByCodeCommand(ShortCode meetingReference, ShortCode confirmationCode,
                                               AttendanceReply attendanceReply, String callerId) {
}
