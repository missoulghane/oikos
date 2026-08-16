package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.query.GetMeetingMinutesQuery;

public interface GetMeetingMinutesUseCase {

    MeetingMinutesView getMinutes(GetMeetingMinutesQuery query);
}
