package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;

/** No result field: the tally is computed on demand from the votes (lot 4), never carried here. */
public record AgendaItemView(AgendaItemId id, GeneralMeetingId generalMeetingId, String label, String description,
                              int position, MajorityRule majorityRule, VoteSessionStatus voteSessionStatus,
                              Instant createdDate) {

    public static AgendaItemView from(AgendaItem item) {
        return new AgendaItemView(item.getId(), item.getGeneralMeetingId(), item.getLabel(), item.getDescription(),
                item.getPosition(), item.getMajorityRule(), item.getVoteSessionStatus(), item.getCreatedDate());
    }
}
