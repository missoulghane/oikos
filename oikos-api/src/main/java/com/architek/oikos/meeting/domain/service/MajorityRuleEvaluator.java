package com.architek.oikos.meeting.domain.service;

import java.math.BigDecimal;

import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.meeting.domain.valueobject.VoteTally;

/**
 * Decides whether an agenda item passed. A pure function over an already
 * computed tally - no repository, no clock, nothing to mock - because this is
 * the single place in the module where a arguable reading of the law lives,
 * and it has to be readable and testable on its own (ADR 0002 §4).
 *
 * <p>The three rules differ only by their denominator, which is exactly why
 * VoteTally carries all three:
 *
 * <ul>
 *   <li>{@code SIMPLE} - more voices for than against, abstentions ignored.
 *       The usual rule for ordinary business.</li>
 *   <li>{@code ABSOLUTE} - more than half the voices of the whole
 *       copropriété, whether those lots turned up or not. The strict reading
 *       of article 21 of loi 18-00: a decision binding every copropriétaire
 *       is measured against every copropriétaire, so lots that stayed home
 *       count as not having supported it. This is the one line to change if
 *       the other reading (half of the lots present) is ever preferred.</li>
 *   <li>{@code UNANIMITY} - among the lots present, no opposition and no
 *       abstention, and at least one voice in favour. That last condition is
 *       not pedantry: without it, an item nobody voted on at all would come
 *       out "adopted unanimously".</li>
 * </ul>
 *
 * <p>Nothing here looks at whether the ballot is closed. A tally read mid-session
 * gives the standing of the vote at that instant, which is what the chair
 * needs to see; the result only becomes final when the minutes freeze it.
 */
public final class MajorityRuleEvaluator {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private MajorityRuleEvaluator() {
    }

    public static VoteOutcome evaluate(MajorityRule rule, VoteTally tally) {
        return isAdopted(rule, tally) ? VoteOutcome.ADOPTED : VoteOutcome.REJECTED;
    }

    private static boolean isAdopted(MajorityRule rule, VoteTally tally) {
        BigDecimal forWeight = tally.forWeight().value();
        BigDecimal againstWeight = tally.againstWeight().value();
        BigDecimal abstentionWeight = tally.abstentionWeight().value();

        return switch (rule) {
            case SIMPLE -> forWeight.compareTo(againstWeight) > 0;
            case ABSOLUTE -> forWeight.compareTo(tally.totalWeight().value().divide(TWO, forWeight.scale() + 2,
                    java.math.RoundingMode.HALF_UP)) > 0;
            case UNANIMITY -> tally.presentWeight().value().signum() > 0
                    && forWeight.signum() > 0
                    && againstWeight.signum() == 0
                    && abstentionWeight.signum() == 0;
        };
    }
}
