package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.PublishMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;

public interface PublishMeetingMinutesUseCase {

    MeetingMinutesView publish(PublishMeetingMinutesCommand command);
}
