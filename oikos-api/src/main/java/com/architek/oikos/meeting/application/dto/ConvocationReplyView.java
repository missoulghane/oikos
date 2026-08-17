package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;

/**
 * One answer, as a screen shows it. mediumLabel is resolved from the catalog
 * here rather than left to the client, for the same reason a delivery's
 * channelLabel is: the label is the catalog's to define, and a front-end map of
 * codes to labels would have to be redeployed every time one is added.
 *
 * <p>receivedAt is when the answer was given, recordedAt when it was typed.
 * Both ship, because they are what makes a backdated entry legible - "reçue le
 * 13, saisie le 15" - and the second is the tiebreak when two answers claim the
 * same instant.
 */
public record ConvocationReplyView(String id, AttendanceReply attendanceReply, AttendanceMode attendanceMode,
                                    boolean byProxy, ReplySource source, String mediumCode, String mediumLabel,
                                    String note, Instant receivedAt, Instant recordedAt) {
}
