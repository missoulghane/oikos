package com.architek.oikos.meeting.web.request;

/**
 * forceWithoutQuorum defaults to false: opening a session the quorum does not
 * carry has to be asked for, never inherited from an empty body.
 */
public record OpenGeneralMeetingRequest(boolean forceWithoutQuorum) {
}
