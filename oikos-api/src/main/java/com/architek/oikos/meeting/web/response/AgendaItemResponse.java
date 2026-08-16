package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;

public record AgendaItemResponse(String id, String generalMeetingId, String label, String description, int position,
                                  MajorityRule majorityRule, VoteSessionStatus voteSessionStatus, Instant createdDate) {

    public static AgendaItemResponse from(AgendaItemView view) {
        return new AgendaItemResponse(view.id().toString(), view.generalMeetingId().toString(), view.label(),
                view.description(), view.position(), view.majorityRule(), view.voteSessionStatus(), view.createdDate());
    }
}
