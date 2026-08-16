package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.DeleteAgendaItemCommand;

public interface DeleteAgendaItemUseCase {

    void delete(DeleteAgendaItemCommand command);
}
