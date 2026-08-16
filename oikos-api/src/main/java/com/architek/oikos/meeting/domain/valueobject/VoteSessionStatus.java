package com.architek.oikos.meeting.domain.valueobject;

/**
 * Whether voting on an agenda item is open. A genuine session state, not a
 * derivation - which is exactly why it is stored while the result of the vote
 * is not: "has the chair opened the ballot" cannot be recomputed from the
 * votes cast, whereas the tally can.
 */
public enum VoteSessionStatus {
    NOT_OPENED,
    OPEN,
    CLOSED
}
