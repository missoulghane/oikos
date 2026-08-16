package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;

public interface AddAgendaItemUseCase {

    AgendaItemView add(AddAgendaItemCommand command);
}
