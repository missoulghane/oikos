package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.VoteChoice;

/** unitId, not partyId: the lot votes, whoever happens to hold it. */
public record CastVoteRequest(@NotBlank String unitId, @NotNull VoteChoice choice) {
}
