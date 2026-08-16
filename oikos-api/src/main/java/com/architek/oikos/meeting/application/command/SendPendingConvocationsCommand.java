package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Sends every convocation of the meeting still waiting to go out (no delivery
 * recorded at all, or none that worked). Re-running it after a partial failure
 * retries exactly those, and never re-sends one that already left.
 *
 * <p>requestedByUserId owns the generated PDFs: document.uploaded_by is a
 * foreign key onto app_user. It is also what each delivery records as its
 * author.
 */
public record SendPendingConvocationsCommand(GeneralMeetingId generalMeetingId, ChannelCode channel,
                                              EntityId requestedByUserId) {
}
