package com.architek.oikos.meeting.domain.valueobject;

/**
 * Physical, remote or both. The two predicates below are what MeetingVenue
 * enforces its invariant with - kept on the enum so that adding a venue type
 * later states its own requirements instead of extending a condition
 * elsewhere.
 */
public enum VenueType {
    PHYSICAL,
    VIDEOCONFERENCE,
    HYBRID;

    public boolean requiresAddress() {
        return this == PHYSICAL || this == HYBRID;
    }

    public boolean requiresLink() {
        return this == VIDEOCONFERENCE || this == HYBRID;
    }
}
