package com.architek.oikos.meeting.domain.valueobject;

/**
 * Whether an agenda item passed. Computed from the tally and the item's own
 * majority rule at read time, never stored - and frozen exactly once, in the
 * minutes.
 */
public enum VoteOutcome {
    ADOPTED,
    REJECTED
}
