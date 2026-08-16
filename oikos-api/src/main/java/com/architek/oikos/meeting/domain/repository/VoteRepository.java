package com.architek.oikos.meeting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface VoteRepository {

    Vote save(Vote vote);

    List<Vote> saveAll(List<Vote> votes);

    /** The existing vote of one lot on one item, if it has already cast - what makes recasting an update. */
    Optional<Vote> findByAgendaItemIdAndUnitId(AgendaItemId agendaItemId, EntityId unitId);

    /**
     * Every vote on one item. Unpaged: a tally is a sum over the whole set,
     * bounded by the number of lots in one copropriété.
     */
    List<Vote> findByAgendaItemId(AgendaItemId agendaItemId);
}
