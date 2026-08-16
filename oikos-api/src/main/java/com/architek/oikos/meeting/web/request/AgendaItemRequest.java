package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.MajorityRule;

/**
 * Shared by creation and update - an agenda item has no field that is settable
 * only once. majorityRule is required: an item nobody assigned a majority to
 * cannot be decided, and defaulting it silently would decide it wrongly.
 */
public record AgendaItemRequest(@NotBlank @Size(max = 200) String label,
                                 @Size(max = 4000) String description,
                                 @NotNull MajorityRule majorityRule) {
}
