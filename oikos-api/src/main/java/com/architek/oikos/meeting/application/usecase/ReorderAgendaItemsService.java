package com.architek.oikos.meeting.application.usecase;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ReorderAgendaItemsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.ReorderAgendaItemsUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Takes the agenda in its new order, in full, and assigns positions 0..n-1.
 *
 * <p>The command must list every item of the meeting exactly once. A partial
 * list is rejected rather than interpreted: "move item 3 to the top" and
 * "here are the only items that remain" look identical on the wire, and
 * guessing between them would silently drop points from an agenda.
 *
 * <p>The moves are saved together, in one transaction: a permutation goes
 * through duplicate positions midway, and uk_agenda_item_position is
 * DEFERRABLE precisely so that this is checked at commit instead.
 */
@Component
public class ReorderAgendaItemsService implements ReorderAgendaItemsUseCase {

    private final AgendaItemRepository agendaItemRepository;
    private final GeneralMeetingRepository generalMeetingRepository;

    public ReorderAgendaItemsService(AgendaItemRepository agendaItemRepository,
                                      GeneralMeetingRepository generalMeetingRepository) {
        this.agendaItemRepository = agendaItemRepository;
        this.generalMeetingRepository = generalMeetingRepository;
    }

    @Override
    @Transactional
    public List<AgendaItemView> reorder(ReorderAgendaItemsCommand command) {
        generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        Map<AgendaItemId, AgendaItem> current = agendaItemRepository
                .findByGeneralMeetingId(command.generalMeetingId()).stream()
                .collect(Collectors.toMap(AgendaItem::getId, Function.identity()));

        Set<AgendaItemId> requested = new LinkedHashSet<>(command.orderedItemIds());
        if (requested.size() != command.orderedItemIds().size() || !requested.equals(current.keySet())) {
            throw new BusinessException("The new order must list every agenda item of the meeting exactly once");
        }

        List<AgendaItem> reordered = IntStream.range(0, command.orderedItemIds().size())
                .mapToObj(index -> current.get(command.orderedItemIds().get(index)).moveTo(index))
                .toList();

        return agendaItemRepository.saveAll(reordered).stream().map(AgendaItemView::from).toList();
    }
}
