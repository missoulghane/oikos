package com.architek.oikos.meeting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public interface AgendaItemRepository {

    AgendaItem save(AgendaItem agendaItem);

    /**
     * Saves a whole reordered agenda in one go. Separate from save() because
     * a reordering necessarily passes through duplicate positions midway, and
     * the unique constraint that guards them is DEFERRABLE: the moves have to
     * land in a single transaction, checked at commit.
     */
    List<AgendaItem> saveAll(List<AgendaItem> agendaItems);

    Optional<AgendaItem> findById(AgendaItemId id);

    void deleteById(AgendaItemId id);

    /** The agenda in reading order (position ascending). */
    List<AgendaItem> findByGeneralMeetingId(GeneralMeetingId generalMeetingId);

    long countByGeneralMeetingId(GeneralMeetingId generalMeetingId);

    /** Highest position currently used, empty on an agenda with no item yet. */
    Optional<Integer> findMaxPosition(GeneralMeetingId generalMeetingId);
}
