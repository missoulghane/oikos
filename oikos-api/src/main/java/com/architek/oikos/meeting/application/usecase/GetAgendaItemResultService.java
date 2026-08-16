package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemResultUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemResultQuery;
import com.architek.oikos.meeting.domain.model.AgendaItem;

/**
 * The tally, recomputed on every read. Available at any point - before the
 * ballot opens (all zeros), while it is open (where the vote stands), and
 * after it closes (the result). Nothing is stored, so it can never contradict
 * the votes it is made of.
 */
@Component
public class GetAgendaItemResultService implements GetAgendaItemResultUseCase {

    private final BallotContext ballotContext;

    public GetAgendaItemResultService(BallotContext ballotContext) {
        this.ballotContext = ballotContext;
    }

    @Override
    @Transactional(readOnly = true)
    public AgendaItemResultView getResult(GetAgendaItemResultQuery query) {
        AgendaItem item = ballotContext.requireAgendaItem(query.agendaItemId());
        return ballotContext.resultOf(item, ballotContext.convocationsOf(ballotContext.requireMeetingOf(item)));
    }
}
