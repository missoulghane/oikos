package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.GenerateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;

public interface GenerateMeetingMinutesUseCase {

    MeetingMinutesView generate(GenerateMeetingMinutesCommand command);
}
