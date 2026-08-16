package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.UpdateAgendaItemCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.UpdateAgendaItemUseCase;
import com.architek.oikos.meeting.domain.exception.AgendaItemNotFoundException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

@Component
public class UpdateAgendaItemService implements UpdateAgendaItemUseCase {

    private final AgendaItemRepository agendaItemRepository;

    public UpdateAgendaItemService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional
    public AgendaItemView update(UpdateAgendaItemCommand command) {
        AgendaItem item = agendaItemRepository.findById(command.id())
                .orElseThrow(() -> new AgendaItemNotFoundException(command.id()));
        AgendaItem updated = item.update(command.label(), command.description(), command.majorityRule());
        return AgendaItemView.from(agendaItemRepository.save(updated));
    }
}
