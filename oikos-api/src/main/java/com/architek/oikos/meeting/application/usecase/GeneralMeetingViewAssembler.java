package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds GeneralMeetingViews, resolving the two things the aggregate does not
 * carry: the property's display name and the agenda item count. Shared by
 * every service that returns a view, so the PropertyDirectoryPort crossing is
 * written once.
 *
 * <p>The list variant caches property names per call: a page of meetings
 * belongs to a single property in practice, and going through the port once
 * per row would turn one screen into twenty cross-module calls.
 */
@Component
public class GeneralMeetingViewAssembler {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final AgendaItemRepository agendaItemRepository;

    public GeneralMeetingViewAssembler(PropertyDirectoryPort propertyDirectoryPort,
                                        AgendaItemRepository agendaItemRepository) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.agendaItemRepository = agendaItemRepository;
    }

    public GeneralMeetingView toView(GeneralMeeting meeting) {
        return GeneralMeetingView.from(meeting, propertyDirectoryPort.getProperty(meeting.getPropertyId()).name(),
                agendaItemRepository.countByGeneralMeetingId(meeting.getId()));
    }

    public List<GeneralMeetingView> toViews(List<GeneralMeeting> meetings) {
        Map<EntityId, String> nameByProperty = new HashMap<>();
        return meetings.stream()
                .map(meeting -> GeneralMeetingView.from(meeting,
                        nameByProperty.computeIfAbsent(meeting.getPropertyId(),
                                propertyId -> propertyDirectoryPort.getProperty(propertyId).name()),
                        agendaItemRepository.countByGeneralMeetingId(meeting.getId())))
                .toList();
    }
}
