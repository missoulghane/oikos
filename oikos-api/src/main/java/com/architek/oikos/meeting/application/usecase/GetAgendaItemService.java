package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemQuery;
import com.architek.oikos.meeting.domain.exception.AgendaItemNotFoundException;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

@Component
public class GetAgendaItemService implements GetAgendaItemUseCase {

    private final AgendaItemRepository agendaItemRepository;

    public GetAgendaItemService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgendaItemView getAgendaItem(GetAgendaItemQuery query) {
        return AgendaItemView.from(agendaItemRepository.findById(query.id())
                .orElseThrow(() -> new AgendaItemNotFoundException(query.id())));
    }
}
