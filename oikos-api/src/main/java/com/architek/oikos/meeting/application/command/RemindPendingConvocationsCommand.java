package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RemindPendingConvocationsCommand(GeneralMeetingId generalMeetingId, EntityId requestedByUserId) {
}
