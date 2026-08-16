package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Opening a ballot already open, or closing one never opened. A 409 for the
 * same reason as InvalidMeetingStatusTransitionException: the request was
 * legal when the page was rendered, the item has moved on since.
 *
 * <p>A closed ballot never reopens - reopening would let a result already
 * announced to the room be changed afterwards.
 */
public class InvalidVoteSessionTransitionException extends ConflictException {

    public InvalidVoteSessionTransitionException(VoteSessionStatus from, VoteSessionStatus to) {
        super("The vote session of an agenda item cannot go from " + from + " to " + to);
    }
}
