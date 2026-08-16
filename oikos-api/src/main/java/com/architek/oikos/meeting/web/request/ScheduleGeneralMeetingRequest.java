package com.architek.oikos.meeting.web.request;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.VenueType;

/** Date and venue are required here, unlike on the draft - scheduling is what makes them mandatory. */
public record ScheduleGeneralMeetingRequest(@NotNull Instant scheduledAt,
                                             @NotNull VenueType venueType,
                                             @Size(max = 250) String venueAddress,
                                             @Size(max = 500) String venueLink) {
}
