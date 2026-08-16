package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;

public interface ScheduleGeneralMeetingUseCase {

    GeneralMeetingView schedule(ScheduleGeneralMeetingCommand command);
}
