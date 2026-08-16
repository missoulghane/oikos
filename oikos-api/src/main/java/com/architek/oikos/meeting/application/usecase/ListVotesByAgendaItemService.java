package com.architek.oikos.meeting.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.VoteView;
import com.architek.oikos.meeting.application.port.in.ListVotesByAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.application.query.ListVotesByAgendaItemQuery;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The nominal vote sheet: who voted what, with each lot's label and weight.
 * This is what the minutes reproduce when a copropriétaire asks to have their
 * opposition recorded.
 */
@Component
public class ListVotesByAgendaItemService implements ListVotesByAgendaItemUseCase {

    private final BallotContext ballotContext;
    private final VoteRepository voteRepository;
    private final ConvocationViewAssembler convocationViewAssembler;

    public ListVotesByAgendaItemService(BallotContext ballotContext, VoteRepository voteRepository,
                                         ConvocationViewAssembler convocationViewAssembler) {
        this.ballotContext = ballotContext;
        this.voteRepository = voteRepository;
        this.convocationViewAssembler = convocationViewAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteView> listVotes(ListVotesByAgendaItemQuery query) {
        AgendaItem item = ballotContext.requireAgendaItem(query.agendaItemId());
        GeneralMeeting meeting = ballotContext.requireMeetingOf(item);

        Map<EntityId, UnitInfo> unitsById = convocationViewAssembler.unitsByIdOf(meeting.getPropertyId());
        Map<EntityId, Convocation> byUnit = ballotContext.convocationsByUnit(meeting);

        return voteRepository.findByAgendaItemId(item.getId()).stream().map(vote -> {
            UnitInfo unit = unitsById.get(vote.getUnitId());
            Convocation convocation = byUnit.get(vote.getUnitId());
            return VoteView.from(vote, unit == null ? null : unit.unitNumber(),
                    unit == null ? null : unit.buildingName(),
                    convocation == null ? BigDecimal.ZERO : convocation.getVotingWeight().value());
        }).toList();
    }
}
