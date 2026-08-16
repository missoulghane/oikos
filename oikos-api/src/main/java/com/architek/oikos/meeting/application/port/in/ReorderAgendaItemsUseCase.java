package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.command.ReorderAgendaItemsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;

public interface ReorderAgendaItemsUseCase {

    List<AgendaItemView> reorder(ReorderAgendaItemsCommand command);
}
