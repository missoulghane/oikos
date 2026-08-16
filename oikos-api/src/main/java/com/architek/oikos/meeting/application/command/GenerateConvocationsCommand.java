package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

/**
 * Generating carries no channel and no sender: it creates one convocation per
 * lot and moves the meeting to CONVENED, nothing more. Sending is a separate
 * act (SendPendingConvocationsCommand) - the syndic generates, checks the
 * tracking table, and only then sends.
 */
public record GenerateConvocationsCommand(GeneralMeetingId generalMeetingId) {
}
