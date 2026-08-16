package com.architek.oikos.meeting.application.command;

import java.util.List;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

/** orderedItemIds is the agenda in its new reading order, in full - not a delta. */
public record ReorderAgendaItemsCommand(GeneralMeetingId generalMeetingId, List<AgendaItemId> orderedItemIds) {
}
