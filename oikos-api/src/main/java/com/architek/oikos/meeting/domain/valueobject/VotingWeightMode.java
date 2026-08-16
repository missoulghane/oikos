package com.architek.oikos.meeting.domain.valueobject;

/**
 * How much a lot's vote weighs. This module's own copy of the property's dues
 * calculation mode (FLAT_RATE -&gt; PER_UNIT, SHARES -&gt; SHARES): meeting must
 * not depend on property.domain.valueobject.DuesCalculationMode directly
 * (rule 4), exactly as installment already keeps its own copy of that enum.
 * The translation happens in MeetingPropertyDirectoryAdapter, the only class
 * that knows both.
 *
 * <p>Named after what it means here rather than after the dues concept it is
 * derived from: a copropriété that bills a flat rate per lot type also votes
 * one-lot-one-voice, but the reason to call it PER_UNIT is the vote, not the
 * billing.
 */
public enum VotingWeightMode {
    /** One voice per lot, whatever its size. */
    PER_UNIT,
    /** Voice proportional to the lot's tantièmes (Unit.shares). */
    SHARES
}
