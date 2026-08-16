package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;

/**
 * Appends a point at the end of the agenda. Positions are dense and
 * zero-based, so the next one is max + 1. Unguarded by status: the agenda is
 * deliberately never frozen for the time being (ADR 0002 §8).
 */
@Component
public class AddAgendaItemService implements AddAgendaItemUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final AgendaItemRepository agendaItemRepository;

    public AddAgendaItemService(GeneralMeetingRepository generalMeetingRepository,
                                 AgendaItemRepository agendaItemRepository) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional
    public AgendaItemView add(AddAgendaItemCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        int position = agendaItemRepository.findMaxPosition(meeting.getId()).map(max -> max + 1).orElse(0);
        AgendaItem created = AgendaItem.create(AgendaItemId.newId(), meeting.getId(), command.label(),
                command.description(), position, command.majorityRule());

        return AgendaItemView.from(agendaItemRepository.save(created));
    }
}
