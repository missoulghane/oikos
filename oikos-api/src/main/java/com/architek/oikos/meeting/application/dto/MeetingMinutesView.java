package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingMinutesId;
import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;

public record MeetingMinutesView(MeetingMinutesId id, GeneralMeetingId generalMeetingId, String content,
                                  MinutesStatus status, Instant publishedAt, Instant createdDate) {

    public static MeetingMinutesView from(MeetingMinutes minutes) {
        return new MeetingMinutesView(minutes.getId(), minutes.getGeneralMeetingId(), minutes.getContent(),
                minutes.getStatus(), minutes.getPublishedAt(), minutes.getCreatedDate());
    }
}
