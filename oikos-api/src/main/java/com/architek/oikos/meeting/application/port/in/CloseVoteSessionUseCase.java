package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.CloseVoteSessionCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;

/** Returns the result as of closing - the figures the chair announces to the room. */
public interface CloseVoteSessionUseCase {

    AgendaItemResultView close(CloseVoteSessionCommand command);
}
