package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * propertyName is supplied by the caller (resolved through
 * PropertyDirectoryPort) rather than looked up by whoever consumes the view,
 * same pattern as UnitView.unitTypeName - the list screens all show it.
 * agendaItemCount likewise: it is what tells a syndic at a glance whether a
 * draft is ready to be scheduled.
 *
 * <p>comment is rich-text HTML written by the syndic and read by
 * copropriétaires. Whatever displays it sanitizes it first - it is user input,
 * and the product's own convention for that is renderMessageBody's DOMPurify
 * pass on the web side.
 */
public record GeneralMeetingView(GeneralMeetingId id, EntityId propertyId, String propertyName, MeetingType meetingType,
                                  MeetingStatus status, String title, Instant scheduledAt, VenueType venueType,
                                  String venueAddress, String venueLink, BigDecimal quorumPercentage,
                                  VotingWeightMode votingWeightMode, String comment, boolean openedWithoutQuorum,
                                  long agendaItemCount, Instant createdDate) {

    public static GeneralMeetingView from(GeneralMeeting meeting, String propertyName, long agendaItemCount) {
        return new GeneralMeetingView(meeting.getId(), meeting.getPropertyId(), propertyName, meeting.getMeetingType(),
                meeting.getStatus(), meeting.getTitle(), meeting.getScheduledAt(),
                meeting.getVenue() != null ? meeting.getVenue().type() : null,
                meeting.getVenue() != null ? meeting.getVenue().address() : null,
                meeting.getVenue() != null ? meeting.getVenue().link() : null,
                meeting.getQuorumPercentage().value(), meeting.getVotingWeightMode(), meeting.getComment(),
                meeting.isOpenedWithoutQuorum(), agendaItemCount, meeting.getCreatedDate());
    }
}
