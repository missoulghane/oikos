package com.architek.oikos.meeting.domain.repository;

import java.util.Optional;

import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public interface MeetingMinutesRepository {

    MeetingMinutes save(MeetingMinutes minutes);

    /** Keyed by the meeting, not by its own id: the relation is 1-1 and every caller comes from the meeting. */
    Optional<MeetingMinutes> findByGeneralMeetingId(GeneralMeetingId generalMeetingId);
}
