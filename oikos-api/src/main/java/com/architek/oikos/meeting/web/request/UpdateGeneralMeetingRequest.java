package com.architek.oikos.meeting.web.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * Full replacement of the editable fields (hence PUT, not PATCH): omitting
 * scheduledAt or the venue clears them, which is what a draft being walked
 * back to "date not decided yet" actually means.
 *
 * <p>The venue is three flat fields rather than a nested object because that
 * is how the form is filled in; MeetingVenue enforces which of them are
 * required for the chosen type.
 */
public record UpdateGeneralMeetingRequest(@NotNull MeetingType meetingType,
                                           @NotBlank @Size(max = 200) String title,
                                           Instant scheduledAt,
                                           VenueType venueType,
                                           @Size(max = 250) String venueAddress,
                                           @Size(max = 500) String venueLink) {
}
