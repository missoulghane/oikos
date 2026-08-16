package com.architek.oikos.meeting.domain.valueobject;

import java.util.Set;

/**
 * The six states of a general meeting, in the order the SFD lays them out.
 * Unlike ConvocationStatus (derived) or an installment's status (computed at
 * read time), this is a real persisted state: nothing else in the model lets
 * you tell a meeting that has been convened from one that merely has a date.
 *
 * <p>The allowed transitions live here rather than in a switch inside
 * GeneralMeeting, so that the graph can be read - and tested - in one place.
 * There is no way back: a convocation already sent cannot be unsent, and a
 * published record of a meeting cannot be un-published. Correcting a meeting
 * that went wrong means creating another one, which is also how it works
 * outside the software.
 */
public enum MeetingStatus {

    /** Being prepared, not yet convoked. Editing is not restricted to it - see GeneralMeeting.update. */
    DRAFT,
    /** Date and venue settled; ready to convoke. Nothing is frozen - see ADR 0002 §8. */
    SCHEDULED,
    /** Convocations have been generated (and, for the automatable channels, sent). */
    CONVENED,
    /** Session open: owners check in and votes are cast. */
    IN_PROGRESS,
    /** Session over; the minutes can be drafted. */
    CLOSED,
    /** Minutes published to the owners - end of the cycle. */
    MINUTES_PUBLISHED;

    public boolean canTransitionTo(MeetingStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<MeetingStatus> allowedTargets() {
        return switch (this) {
            case DRAFT -> Set.of(SCHEDULED);
            case SCHEDULED -> Set.of(CONVENED);
            case CONVENED -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(CLOSED);
            case CLOSED -> Set.of(MINUTES_PUBLISHED);
            case MINUTES_PUBLISHED -> Set.of();
        };
    }
}
