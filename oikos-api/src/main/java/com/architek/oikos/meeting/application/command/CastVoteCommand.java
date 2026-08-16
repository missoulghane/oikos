package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** unitId, not partyId: the lot votes (ADR 0002 §2). */
public record CastVoteCommand(AgendaItemId agendaItemId, EntityId unitId, VoteChoice choice, EntityId castByUserId) {
}
