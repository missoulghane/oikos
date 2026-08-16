package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.CloseVoteSessionCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.port.in.CloseVoteSessionUseCase;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

/**
 * Closes the ballot and returns the result - the figures the chair announces.
 *
 * <p>A closed ballot never reopens (AgendaItem refuses it): a result announced
 * to the room cannot be changed by collecting more votes afterwards.
 */
@Component
public class CloseVoteSessionService implements CloseVoteSessionUseCase {

    private final BallotContext ballotContext;
    private final AgendaItemRepository agendaItemRepository;

    public CloseVoteSessionService(BallotContext ballotContext, AgendaItemRepository agendaItemRepository) {
        this.ballotContext = ballotContext;
        this.agendaItemRepository = agendaItemRepository;
    }

    @Override
    @Transactional
    public AgendaItemResultView close(CloseVoteSessionCommand command) {
        AgendaItem item = ballotContext.requireAgendaItem(command.agendaItemId());
        GeneralMeeting meeting = ballotContext.requireMeetingOf(item);
        ballotContext.requireMeetingInProgress(meeting);

        AgendaItem closed = agendaItemRepository.save(item.closeVoteSession());
        return ballotContext.resultOf(closed, ballotContext.convocationsOf(meeting));
    }
}
