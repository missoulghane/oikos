package com.architek.oikos.meeting.application.command;

import java.util.Map;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A show of hands, as it is actually announced in the room: "adopted, with
 * lots 4 and 12 against and lot 7 abstaining". defaultChoice applies to every
 * lot present, exceptions override it lot by lot.
 *
 * <p>Modelled this way rather than as a list of one vote per lot because that
 * is what the chair says and what the secretary types - and because a
 * hundred-lot copropriété would otherwise mean a hundred-row payload for a
 * decision that took four seconds.
 */
public record RecordShowOfHandsCommand(AgendaItemId agendaItemId, VoteChoice defaultChoice,
                                        Map<EntityId, VoteChoice> exceptions, EntityId castByUserId) {
}
