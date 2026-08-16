package com.architek.oikos.meeting.domain.valueobject;

/**
 * NO_REPLY is a value, not the absence of one: "has not answered yet" is a
 * state of the convocation's journey, and it is what the reminder run selects
 * on.
 */
public enum AttendanceReply {
    ATTENDING,
    NOT_ATTENDING,
    NO_REPLY
}
