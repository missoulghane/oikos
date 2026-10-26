package com.architek.oikos.meeting.application.command;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * scheduledAt and venue are optional: the aggregate allows a draft to carry
 * neither, and the API keeps that door open (a syndic scripting a series of
 * AGs may well fill them in later). The web form asks for them up front all
 * the same - in practice a date is decided with the assembly, not after it.
 *
 * <p>comment is optional too: the syndic often has the note of intent in hand
 * when opening the assembly, and writing it there saves a trip to the
 * Informations tab. Blank is normalised to null by the aggregate.
 */
public record CreateGeneralMeetingCommand(EntityId propertyId, MeetingType meetingType, String title,
                                           Instant scheduledAt, MeetingVenue venue, String comment) {
}
