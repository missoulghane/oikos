package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.meeting.application.dto.VoteView;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;

public record VoteResponse(String id, String agendaItemId, String unitId, String unitNumber, String buildingName,
                            BigDecimal votingWeight, VoteChoice choice, Instant castAt) {

    public static VoteResponse from(VoteView view) {
        return new VoteResponse(view.id().toString(), view.agendaItemId().toString(), view.unitId().toString(),
                view.unitNumber(), view.buildingName(), view.votingWeight(), view.choice(), view.castAt());
    }
}
