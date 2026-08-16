package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** checkedInPartyId is optional - a syndic ticking off a room does not always know which co-owner came. */
public record CheckInConvocationCommand(ConvocationId convocationId, AttendanceMode attendanceMode,
                                         EntityId checkedInPartyId) {
}
