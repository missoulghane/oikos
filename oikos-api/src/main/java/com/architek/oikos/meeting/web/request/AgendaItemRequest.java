package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.MajorityRule;

/**
 * Shared by creation and update - an agenda item has no field that is settable
 * only once. majorityRule is required: an item nobody assigned a majority to
 * cannot be decided, and defaulting it silently would decide it wrongly.
 *
 * <p>The description is capped at 20 000 like a meeting's comment, not at the
 * 4 000 it used to be. The column is `text`, so the old cap protected nothing;
 * what it did do was refuse a resolution written out in full - the terms of a
 * works contract, a budget line by line - which is precisely what a point of an
 * agenda has to carry, since it is put to the vote as it is worded here.
 */
public record AgendaItemRequest(@NotBlank @Size(max = 200) String label,
                                 @Size(max = 20000) String description,
                                 @NotNull MajorityRule majorityRule) {
}
