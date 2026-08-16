package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.DeleteGeneralMeetingCommand;

public interface DeleteGeneralMeetingUseCase {

    void delete(DeleteGeneralMeetingCommand command);
}
