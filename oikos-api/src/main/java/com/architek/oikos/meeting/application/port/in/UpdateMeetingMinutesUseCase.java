package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.UpdateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;

public interface UpdateMeetingMinutesUseCase {

    MeetingMinutesView update(UpdateMeetingMinutesCommand command);
}
