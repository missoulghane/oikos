package com.architek.oikos.meeting.application.port.out;

/**
 * Caps how often a wrong confirmation code may be tried, per meeting and per
 * caller.
 *
 * <p>A port and not a utility class, for two reasons that both matter. The
 * counting is a technical concern - today an in-memory map, tomorrow Redis or a
 * table once the product runs on more than one instance - and the application
 * layer must not import either. And the day it moves, this interface is the
 * whole of what has to stay true.
 *
 * <p>It is not an ergonomic nicety: the six-character code is about 34^6, six
 * orders of magnitude below the convocation token. A secret that small is only
 * defensible if it cannot be tried in a loop, so this cap is the other half of
 * the decision to offer a short code at all (ADR 0002 §13).
 */
public interface ConfirmationAttemptLimiterPort {

    boolean isAllowed(String meetingReference, String callerId);

    void recordFailure(String meetingReference, String callerId);

    /** A success clears the slate: the caller evidently holds a real code. */
    void recordSuccess(String meetingReference, String callerId);
}
