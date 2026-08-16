package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.dto.VoteView;
import com.architek.oikos.meeting.application.port.in.CastVoteUseCase;
import com.architek.oikos.meeting.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Records one lot's vote on one point - the nominal entry of the SFD.
 *
 * <p>Casting again while the ballot is open replaces the previous choice on
 * the same row: people correct themselves, and the unicity (item, lot) has to
 * hold. Once the ballot closes nothing more is accepted.
 */
@Component
public class CastVoteService implements CastVoteUseCase {

    private final BallotContext ballotContext;
    private final VoteRepository voteRepository;
    private final PropertyUnitDirectoryPort propertyUnitDirectoryPort;
    private final Clock clock;

    public CastVoteService(BallotContext ballotContext, VoteRepository voteRepository,
                            PropertyUnitDirectoryPort propertyUnitDirectoryPort, Clock clock) {
        this.ballotContext = ballotContext;
        this.voteRepository = voteRepository;
        this.propertyUnitDirectoryPort = propertyUnitDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public VoteView cast(CastVoteCommand command) {
        AgendaItem item = ballotContext.requireAgendaItem(command.agendaItemId());
        GeneralMeeting meeting = ballotContext.requireMeetingOf(item);
        ballotContext.requireMeetingInProgress(meeting);
        ballotContext.requireBallotOpen(item);

        UnitInfo unit = propertyUnitDirectoryPort.listUnits(meeting.getPropertyId()).stream()
                .filter(candidate -> candidate.unitId().equals(command.unitId())).findFirst().orElse(null);
        Map<EntityId, Convocation> byUnit = ballotContext.convocationsByUnit(meeting);
        Convocation convocation = ballotContext.requireVotingLot(byUnit, command.unitId(),
                unit == null ? null : unit.unitNumber());

        Vote vote = voteRepository.findByAgendaItemIdAndUnitId(item.getId(), command.unitId())
                .map(existing -> existing.recast(command.choice(), clock.instant(), command.castByUserId()))
                .orElseGet(() -> Vote.cast(VoteId.newId(), item.getId(), command.unitId(), command.choice(),
                        clock.instant(), command.castByUserId()));

        return VoteView.from(voteRepository.save(vote), unit == null ? null : unit.unitNumber(),
                unit == null ? null : unit.buildingName(), convocation.getVotingWeight().value());
    }
}
