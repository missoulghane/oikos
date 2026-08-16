package com.architek.oikos.meeting.web.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/** The agenda in its new reading order, in full - every item exactly once, not a delta. */
public record ReorderAgendaItemsRequest(@NotEmpty List<String> orderedItemIds) {
}
