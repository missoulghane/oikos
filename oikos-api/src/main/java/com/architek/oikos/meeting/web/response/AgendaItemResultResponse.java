package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;

/**
 * The three weights are all exposed, not just the one the applied rule used:
 * a syndic reading a contested result has to be able to see the figures the
 * other readings would have given.
 */
public record AgendaItemResultResponse(String agendaItemId, String label, MajorityRule majorityRule,
                                        VoteSessionStatus voteSessionStatus, int forCount, int againstCount,
                                        int abstentionCount, BigDecimal forWeight, BigDecimal againstWeight,
                                        BigDecimal abstentionWeight, BigDecimal expressedWeight,
                                        BigDecimal presentWeight, BigDecimal totalWeight, VoteOutcome outcome) {

    public static AgendaItemResultResponse from(AgendaItemResultView view) {
        return new AgendaItemResultResponse(view.agendaItemId().toString(), view.label(), view.majorityRule(),
                view.voteSessionStatus(), view.forCount(), view.againstCount(), view.abstentionCount(),
                view.forWeight(), view.againstWeight(), view.abstentionWeight(), view.expressedWeight(),
                view.presentWeight(), view.totalWeight(), view.outcome());
    }
}
