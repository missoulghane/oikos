package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.UpdateAgendaItemCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;

public interface UpdateAgendaItemUseCase {

    AgendaItemView update(UpdateAgendaItemCommand command);
}
