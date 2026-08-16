package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;

public record MeetingMinutesResponse(String id, String generalMeetingId, String content, MinutesStatus status,
                                      Instant publishedAt, Instant createdDate) {

    public static MeetingMinutesResponse from(MeetingMinutesView view) {
        return new MeetingMinutesResponse(view.id().toString(), view.generalMeetingId().toString(), view.content(),
                view.status(), view.publishedAt(), view.createdDate());
    }
}
