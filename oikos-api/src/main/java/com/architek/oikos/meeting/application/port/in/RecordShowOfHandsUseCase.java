package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.RecordShowOfHandsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;

public interface RecordShowOfHandsUseCase {

    AgendaItemResultView record(RecordShowOfHandsCommand command);
}
