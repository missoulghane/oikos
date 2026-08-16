package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.DeleteAgendaItemCommand;
import com.architek.oikos.meeting.application.port.in.DeleteAgendaItemUseCase;
import com.architek.oikos.meeting.domain.exception.AgendaItemNotFoundException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

/**
 * Removes a point and closes the gap it leaves. Positions have to stay dense
 * and zero-based: the reordering endpoint assigns 0..n-1 wholesale, so a hole
 * left here would surface later as a silent renumbering nobody asked for.
 */
@Component
public class DeleteAgendaItemService implements DeleteAgendaItemUseCase {

    private final AgendaItemRepository agendaItemRepository;

    public DeleteAgendaItemService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteAgendaItemCommand command) {
        AgendaItem item = agendaItemRepository.findById(command.id())
                .orElseThrow(() -> new AgendaItemNotFoundException(command.id()));
        agendaItemRepository.deleteById(item.getId());

        List<AgendaItem> remaining = agendaItemRepository.findByGeneralMeetingId(item.getGeneralMeetingId());
        List<AgendaItem> renumbered = java.util.stream.IntStream.range(0, remaining.size())
                .mapToObj(index -> remaining.get(index).moveTo(index))
                .toList();
        agendaItemRepository.saveAll(renumbered);
    }
}
