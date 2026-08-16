package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;

public interface UpdateGeneralMeetingUseCase {

    GeneralMeetingView update(UpdateGeneralMeetingCommand command);
}
