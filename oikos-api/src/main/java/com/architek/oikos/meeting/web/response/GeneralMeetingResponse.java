package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.VenueType;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;

public record GeneralMeetingResponse(String id, String propertyId, String propertyName, MeetingType meetingType,
                                      MeetingStatus status, String title, Instant scheduledAt, VenueType venueType,
                                      String venueAddress, String venueLink, BigDecimal quorumPercentage,
                                      VotingWeightMode votingWeightMode, String comment, boolean openedWithoutQuorum,
                                      long agendaItemCount, Instant createdDate) {

    public static GeneralMeetingResponse from(GeneralMeetingView view) {
        return new GeneralMeetingResponse(view.id().toString(), view.propertyId().toString(), view.propertyName(),
                view.meetingType(), view.status(), view.title(), view.scheduledAt(), view.venueType(),
                view.venueAddress(), view.venueLink(), view.quorumPercentage(), view.votingWeightMode(),
                view.comment(), view.openedWithoutQuorum(), view.agendaItemCount(), view.createdDate());
    }
}
