package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.query.GetAgendaItemQuery;

/** Used by PropertyAccessEvaluator to resolve an agenda item back to its property. */
public interface GetAgendaItemUseCase {

    AgendaItemView getAgendaItem(GetAgendaItemQuery query);
}
