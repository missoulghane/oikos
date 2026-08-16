package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.OpenVoteSessionUseCase;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

/**
 * Opens the ballot on one point. The session must be under way: a ballot
 * opened before the chair opened the meeting would collect votes from a room
 * whose quorum was never assessed.
 */
@Component
public class OpenVoteSessionService implements OpenVoteSessionUseCase {

    private final BallotContext ballotContext;
    private final AgendaItemRepository agendaItemRepository;

    public OpenVoteSessionService(BallotContext ballotContext, AgendaItemRepository agendaItemRepository) {
        this.ballotContext = ballotContext;
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional
    public AgendaItemView open(OpenVoteSessionCommand command) {
        AgendaItem item = ballotContext.requireAgendaItem(command.agendaItemId());
        ballotContext.requireMeetingInProgress(ballotContext.requireMeetingOf(item));
        return AgendaItemView.from(agendaItemRepository.save(item.openVoteSession()));
    }
}
