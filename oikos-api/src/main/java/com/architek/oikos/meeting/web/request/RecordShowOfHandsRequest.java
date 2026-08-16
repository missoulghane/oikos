package com.architek.oikos.meeting.web.request;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.VoteChoice;

/**
 * A show of hands as it is announced: one choice for the room, then the lots
 * that differ. exceptions is keyed by unitId and may be omitted entirely.
 *
 * <p>The default applies to the lots present, never to the whole copropriété -
 * see RecordShowOfHandsService for why inventing voices for absent lots would
 * decide items that were never carried.
 */
public record RecordShowOfHandsRequest(@NotNull VoteChoice defaultChoice, Map<String, VoteChoice> exceptions) {

    public Map<String, VoteChoice> exceptionsOrEmpty() {
        return exceptions == null ? Map.of() : exceptions;
    }
}
