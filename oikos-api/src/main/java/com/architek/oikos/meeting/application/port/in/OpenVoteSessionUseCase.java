package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;

public interface OpenVoteSessionUseCase {

    AgendaItemView open(OpenVoteSessionCommand command);
}
