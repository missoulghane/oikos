package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.domain.exception.EmptyAgendaException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

/**
 * DRAFT -&gt; SCHEDULED, the transition that makes an AG ready to convoke.
 *
 * <p>The "at least one item" rule lives here rather than on the aggregate:
 * GeneralMeeting does not hold its items (see its javadoc), and having it
 * reach into a repository to count them would put persistence inside the
 * domain. This is the one place that already has both.
 */
@Component
public class ScheduleGeneralMeetingService implements ScheduleGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final AgendaItemRepository agendaItemRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public ScheduleGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                          AgendaItemRepository agendaItemRepository,
                                          GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.agendaItemRepository = agendaItemRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional
    public GeneralMeetingView schedule(ScheduleGeneralMeetingCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.id()));
        if (agendaItemRepository.countByGeneralMeetingId(meeting.getId()) == 0) {
            throw new EmptyAgendaException();
        }
        GeneralMeeting scheduled = meeting.schedule(command.scheduledAt(), command.venue());
        return viewAssembler.toView(generalMeetingRepository.save(scheduled));
    }
}
