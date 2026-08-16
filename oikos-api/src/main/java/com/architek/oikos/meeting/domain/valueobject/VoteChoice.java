package com.architek.oikos.meeting.domain.valueobject;

/**
 * An abstention is a real, recorded choice - not the absence of a vote. The
 * two differ in the count: an abstention weighs in the presence and in a
 * unanimity check, while a lot that never voted weighs in neither.
 */
public enum VoteChoice {
    FOR,
    AGAINST,
    ABSTENTION
}
