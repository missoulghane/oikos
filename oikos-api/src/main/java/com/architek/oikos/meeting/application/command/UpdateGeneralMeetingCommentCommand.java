package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

/** The rich-text note of intent, as the editor produced it. */
public record UpdateGeneralMeetingCommentCommand(GeneralMeetingId generalMeetingId, String comment) {
}
