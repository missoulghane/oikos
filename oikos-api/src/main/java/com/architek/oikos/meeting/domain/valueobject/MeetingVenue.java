package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;

/**
 * Where the meeting is held. The invariant is the whole point of the type: a
 * physical meeting needs an address, a remote one needs a joining link, and a
 * hybrid one needs both - a convocation that reaches an owner without telling
 * them where to go is worthless, and a NOT NULL column alone cannot express
 * "which field is required depends on the other one".
 *
 * <p>Mirrored by chk_general_meeting_venue in V4, as a backstop only: this
 * class is the rule.
 */
public record MeetingVenue(VenueType type, String address, String link) {

    private static final int MAX_ADDRESS_LENGTH = 250;
    private static final int MAX_LINK_LENGTH = 500;

    public MeetingVenue {
        Objects.requireNonNull(type, "type must not be null");
        address = blankToNull(address);
        link = blankToNull(link);
        if (type.requiresAddress() && address == null) {
            throw new IllegalArgumentException("a " + type + " meeting must have an address");
        }
        if (type.requiresLink() && link == null) {
            throw new IllegalArgumentException("a " + type + " meeting must have a joining link");
        }
        if (address != null && address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("address must be at most " + MAX_ADDRESS_LENGTH + " characters");
        }
        if (link != null && link.length() > MAX_LINK_LENGTH) {
            throw new IllegalArgumentException("link must be at most " + MAX_LINK_LENGTH + " characters");
        }
    }

    public static MeetingVenue onSite(String address) {
        return new MeetingVenue(VenueType.PHYSICAL, address, null);
    }

    public static MeetingVenue remote(String link) {
        return new MeetingVenue(VenueType.VIDEOCONFERENCE, null, link);
    }

    public static MeetingVenue hybrid(String address, String link) {
        return new MeetingVenue(VenueType.HYBRID, address, link);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
