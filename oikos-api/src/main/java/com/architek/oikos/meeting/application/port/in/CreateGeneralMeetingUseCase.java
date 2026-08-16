package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

public interface CreateGeneralMeetingUseCase {

    GeneralMeetingId create(CreateGeneralMeetingCommand command);
}
