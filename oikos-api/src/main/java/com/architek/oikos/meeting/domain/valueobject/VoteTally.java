package com.architek.oikos.meeting.domain.valueobject;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The result of one agenda item, in voices and in lots.
 *
 * <p>It carries all three denominators rather than the one its own rule needs,
 * and that is deliberate (ADR 0002 §4): a majority rule is then a pure
 * comparison, and changing which denominator a rule uses - the one genuinely
 * arguable point in the whole module - is one line in
 * {@link com.architek.oikos.meeting.domain.service.MajorityRuleEvaluator}
 * rather than a remodelling.
 *
 * <ul>
 *   <li>expressedWeight = FOR + AGAINST, abstentions excluded</li>
 *   <li>presentWeight = every lot signed in, whether it voted or not</li>
 *   <li>totalWeight = every lot of the copropriété, present or not</li>
 * </ul>
 *
 * <p>Computed from the votes and the convocations on every read, never stored.
 * The weight of a vote comes from its lot's convocation - the snapshot taken
 * when the meeting was convened - so a sale or a correction of tantièmes
 * afterwards cannot restate a result.
 */
public record VoteTally(int forCount, int againstCount, int abstentionCount, VotingWeight forWeight,
                         VotingWeight againstWeight, VotingWeight abstentionWeight, VotingWeight expressedWeight,
                         VotingWeight presentWeight, VotingWeight totalWeight) {

    public VoteTally {
        Objects.requireNonNull(forWeight, "forWeight must not be null");
        Objects.requireNonNull(againstWeight, "againstWeight must not be null");
        Objects.requireNonNull(abstentionWeight, "abstentionWeight must not be null");
        Objects.requireNonNull(expressedWeight, "expressedWeight must not be null");
        Objects.requireNonNull(presentWeight, "presentWeight must not be null");
        Objects.requireNonNull(totalWeight, "totalWeight must not be null");
    }

    /**
     * A vote whose lot has no convocation for this meeting is ignored rather
     * than counted at zero: it cannot happen through the use cases (casting
     * requires a checked-in convocation), and if it ever did, counting it as a
     * weightless voice would quietly distort the count instead of simply not
     * being there.
     */
    public static VoteTally of(Collection<Vote> votes, Collection<Convocation> convocations) {
        Map<EntityId, Convocation> byUnit = convocations.stream()
                .collect(Collectors.toMap(Convocation::getUnitId, Function.identity(), (first, second) -> first));

        int forCount = 0;
        int againstCount = 0;
        int abstentionCount = 0;
        VotingWeight forWeight = VotingWeight.zero();
        VotingWeight againstWeight = VotingWeight.zero();
        VotingWeight abstentionWeight = VotingWeight.zero();

        for (Vote vote : votes) {
            Convocation convocation = byUnit.get(vote.getUnitId());
            if (convocation == null) {
                continue;
            }
            VotingWeight weight = convocation.getVotingWeight();
            switch (vote.getChoice()) {
                case FOR -> {
                    forCount++;
                    forWeight = forWeight.plus(weight);
                }
                case AGAINST -> {
                    againstCount++;
                    againstWeight = againstWeight.plus(weight);
                }
                case ABSTENTION -> {
                    abstentionCount++;
                    abstentionWeight = abstentionWeight.plus(weight);
                }
            }
        }

        VotingWeight presentWeight = VotingWeight.zero();
        VotingWeight totalWeight = VotingWeight.zero();
        for (Convocation convocation : convocations) {
            totalWeight = totalWeight.plus(convocation.getVotingWeight());
            if (convocation.isCheckedIn()) {
                presentWeight = presentWeight.plus(convocation.getVotingWeight());
            }
        }

        return new VoteTally(forCount, againstCount, abstentionCount, forWeight, againstWeight, abstentionWeight,
                forWeight.plus(againstWeight), presentWeight, totalWeight);
    }

    public static VoteTally empty() {
        return new VoteTally(0, 0, 0, VotingWeight.zero(), VotingWeight.zero(), VotingWeight.zero(),
                VotingWeight.zero(), VotingWeight.zero(), VotingWeight.zero());
    }

    /** Lots that actually cast something - for, against or an abstention. */
    public int castCount() {
        return forCount + againstCount + abstentionCount;
    }
}
