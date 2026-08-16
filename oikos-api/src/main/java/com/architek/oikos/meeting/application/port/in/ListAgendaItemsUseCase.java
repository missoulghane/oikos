package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.query.ListAgendaItemsQuery;

public interface ListAgendaItemsUseCase {

    List<AgendaItemView> listAgendaItems(ListAgendaItemsQuery query);
}
