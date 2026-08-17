package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.ConvocationReplyView;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;

/**
 * One entry of the answer history. Deliberately carries no repliedByPartyId:
 * the aggregate keeps it, but naming which indivisaire answered on a screen
 * that lists a lot's whole history would spread a personal fact wider than the
 * one place it is needed.
 */
public record ConvocationReplyResponse(String id, AttendanceReply attendanceReply, AttendanceMode attendanceMode,
                                        boolean byProxy, ReplySource source, String mediumCode, String mediumLabel,
                                        String note, Instant receivedAt, Instant recordedAt) {

    public static ConvocationReplyResponse from(ConvocationReplyView view) {
        return new ConvocationReplyResponse(view.id(), view.attendanceReply(), view.attendanceMode(),
                view.byProxy(), view.source(), view.mediumCode(), view.mediumLabel(), view.note(), view.receivedAt(),
                view.recordedAt());
    }
}
