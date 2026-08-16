package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.ListAgendaItemsUseCase;
import com.architek.oikos.meeting.application.query.ListAgendaItemsQuery;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

/** Unpaged: an agenda is a handful of points by nature, and it is read as a whole. */
@Component
public class ListAgendaItemsService implements ListAgendaItemsUseCase {

    private final AgendaItemRepository agendaItemRepository;

    public ListAgendaItemsService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgendaItemView> listAgendaItems(ListAgendaItemsQuery query) {
        return agendaItemRepository.findByGeneralMeetingId(query.generalMeetingId()).stream()
                .map(AgendaItemView::from).toList();
    }
}
