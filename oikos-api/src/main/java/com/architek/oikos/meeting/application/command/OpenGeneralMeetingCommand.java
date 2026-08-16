package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

/**
 * forceWithoutQuorum is the syndic taking responsibility for opening a session
 * the quorum does not carry - a deliberate, recorded act (ADR 0002 §5), never
 * a default.
 */
public record OpenGeneralMeetingCommand(GeneralMeetingId id, boolean forceWithoutQuorum) {
}
