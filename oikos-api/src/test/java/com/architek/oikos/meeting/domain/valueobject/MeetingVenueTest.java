package com.architek.oikos.meeting.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MeetingVenueTest {

    @Test
    void a_physical_meeting_needs_an_address() {
        assertThatThrownBy(() -> new MeetingVenue(VenueType.PHYSICAL, null, "https://meet.example/ag"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("address");
    }

    @Test
    void a_remote_meeting_needs_a_link() {
        assertThatThrownBy(() -> new MeetingVenue(VenueType.VIDEOCONFERENCE, "12 rue des Orangers", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("link");
    }

    @Test
    void a_hybrid_meeting_needs_both() {
        assertThatThrownBy(() -> MeetingVenue.hybrid("12 rue des Orangers", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeetingVenue.hybrid(null, "https://meet.example/ag"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(MeetingVenue.hybrid("12 rue des Orangers", "https://meet.example/ag").type())
                .isEqualTo(VenueType.HYBRID);
    }

    @Test
    void a_blank_field_counts_as_absent_rather_than_as_a_value() {
        // A form that submits an empty string must fail the same way a missing field does,
        // otherwise a convocation goes out with an address of "".
        assertThatThrownBy(() -> new MeetingVenue(VenueType.PHYSICAL, "   ", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void the_unused_field_of_a_venue_stays_null() {
        assertThat(MeetingVenue.onSite("12 rue des Orangers").link()).isNull();
        assertThat(MeetingVenue.remote("https://meet.example/ag").address()).isNull();
    }

    @Test
    void surrounding_whitespace_is_trimmed() {
        assertThat(MeetingVenue.onSite("  12 rue des Orangers  ").address()).isEqualTo("12 rue des Orangers");
    }
}
