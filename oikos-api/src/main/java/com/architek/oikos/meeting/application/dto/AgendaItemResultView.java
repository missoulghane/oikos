package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;
import com.architek.oikos.meeting.domain.valueobject.VoteTally;

/**
 * The result of one agenda item: the counts, the three weights, the rule that
 * was applied and what it concluded.
 *
 * <p>Everything here is recomputed on read. While the ballot is open it shows
 * where the vote stands, which is what the chair announces; it becomes the
 * final word only when the minutes freeze it.
 */
public record AgendaItemResultView(AgendaItemId agendaItemId, String label, MajorityRule majorityRule,
                                    VoteSessionStatus voteSessionStatus, int forCount, int againstCount,
                                    int abstentionCount, BigDecimal forWeight, BigDecimal againstWeight,
                                    BigDecimal abstentionWeight, BigDecimal expressedWeight, BigDecimal presentWeight,
                                    BigDecimal totalWeight, VoteOutcome outcome) {

    public static AgendaItemResultView from(AgendaItemId agendaItemId, String label, MajorityRule majorityRule,
                                             VoteSessionStatus voteSessionStatus, VoteTally tally,
                                             VoteOutcome outcome) {
        return new AgendaItemResultView(agendaItemId, label, majorityRule, voteSessionStatus, tally.forCount(),
                tally.againstCount(), tally.abstentionCount(), tally.forWeight().value(),
                tally.againstWeight().value(), tally.abstentionWeight().value(), tally.expressedWeight().value(),
                tally.presentWeight().value(), tally.totalWeight().value(), outcome);
    }
}
