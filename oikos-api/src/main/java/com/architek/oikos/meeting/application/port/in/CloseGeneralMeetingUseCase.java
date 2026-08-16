package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.CloseGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;

public interface CloseGeneralMeetingUseCase {

    GeneralMeetingView close(CloseGeneralMeetingCommand command);
}
