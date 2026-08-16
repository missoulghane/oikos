package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;

public interface OpenGeneralMeetingUseCase {

    GeneralMeetingView open(OpenGeneralMeetingCommand command);
}
