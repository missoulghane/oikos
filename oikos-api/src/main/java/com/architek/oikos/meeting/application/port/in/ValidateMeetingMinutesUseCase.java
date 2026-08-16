package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.ValidateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;

public interface ValidateMeetingMinutesUseCase {

    MeetingMinutesView validate(ValidateMeetingMinutesCommand command);
}
