package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.query.GetAgendaItemResultQuery;

public interface GetAgendaItemResultUseCase {

    AgendaItemResultView getResult(GetAgendaItemResultQuery query);
}
