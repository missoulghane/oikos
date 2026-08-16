package com.architek.oikos.meeting.web.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * The meeting is still created as a DRAFT: what this adds is the chance to
 * state the date and the venue straight away, rather than creating an empty
 * shell and editing it a second later.
 *
 * <p>Both stay optional here - the aggregate allows a dateless draft, and only
 * scheduling makes them mandatory. Which of venueAddress/venueLink is required
 * for a given venueType is MeetingVenue's rule, not this record's.
 */
public record CreateGeneralMeetingRequest(@NotNull MeetingType meetingType,
                                           @NotBlank @Size(max = 200) String title,
                                           Instant scheduledAt,
                                           VenueType venueType,
                                           @Size(max = 250) String venueAddress,
                                           @Size(max = 500) String venueLink) {
}
