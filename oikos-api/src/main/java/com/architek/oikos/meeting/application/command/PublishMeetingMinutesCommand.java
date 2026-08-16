package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** publishedByUserId owns the generated PDF: document.uploaded_by is a foreign key onto app_user. */
public record PublishMeetingMinutesCommand(GeneralMeetingId generalMeetingId, EntityId publishedByUserId) {
}
