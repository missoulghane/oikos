package com.architek.oikos.meeting.domain.valueobject;

/**
 * Majority required to adopt one agenda item. Chosen item by item, not once
 * per meeting: approving the accounts and amending the règlement de
 * copropriété do not carry the same threshold, and they routinely sit on the
 * same agenda.
 *
 * <p>Each constant names the denominator its evaluation uses - the whole
 * point of the enum, and the thing that decides an outcome. See
 * MajorityRuleEvaluator (lot 4) for the evaluation itself; the tally exposes
 * all three denominators so switching a reading is one line rather than a
 * remodelling.
 */
public enum MajorityRule {
    /** FOR &gt; AGAINST among expressed voices; abstentions are not counted. */
    SIMPLE,
    /** FOR &gt; half of the voices of the whole copropriété, present or not (loi 18-00 art. 21). */
    ABSOLUTE,
    /** No AGAINST and no ABSTENTION among the lots present. */
    UNANIMITY
}
