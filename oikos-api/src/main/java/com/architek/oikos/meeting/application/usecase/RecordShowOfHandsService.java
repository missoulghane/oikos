package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.RecordShowOfHandsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.port.in.RecordShowOfHandsUseCase;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A show of hands: one choice for the room, with named exceptions.
 *
 * <p>Applies only to the lots actually signed in. That is the whole meaning of
 * "the room voted" - a lot that stayed home did not raise a hand, and giving
 * it the default choice would invent voices, which under an ABSOLUTE majority
 * (measured against every lot of the copropriété) would decide items that
 * were never carried.
 *
 * <p>An exception naming a lot that is not present is refused rather than
 * ignored: it means the secretary is looking at a different list from the one
 * in the room, and silently dropping it would hide that.
 */
@Component
public class RecordShowOfHandsService implements RecordShowOfHandsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordShowOfHandsService.class);

    private final BallotContext ballotContext;
    private final VoteRepository voteRepository;
    private final Clock clock;

    public RecordShowOfHandsService(BallotContext ballotContext, VoteRepository voteRepository, Clock clock) {
        this.ballotContext = ballotContext;
        this.voteRepository = voteRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AgendaItemResultView record(RecordShowOfHandsCommand command) {
        AgendaItem item = ballotContext.requireAgendaItem(command.agendaItemId());
        GeneralMeeting meeting = ballotContext.requireMeetingOf(item);
        ballotContext.requireMeetingInProgress(meeting);
        ballotContext.requireBallotOpen(item);

        List<Convocation> convocations = ballotContext.convocationsOf(meeting);
        Map<EntityId, Convocation> byUnit = ballotContext.convocationsByUnit(meeting);
        for (EntityId unitId : command.exceptions().keySet()) {
            ballotContext.requireVotingLot(byUnit, unitId, null);
        }

        Map<EntityId, Vote> existingByUnit = existingVotesByUnit(item);
        List<Vote> votes = new ArrayList<>();
        for (Convocation convocation : convocations) {
            if (!convocation.isCheckedIn()) {
                continue;
            }
            VoteChoice choice = command.exceptions().getOrDefault(convocation.getUnitId(), command.defaultChoice());
            Vote existing = existingByUnit.get(convocation.getUnitId());
            votes.add(existing != null ? existing.recast(choice, clock.instant(), command.castByUserId())
                    : Vote.cast(VoteId.newId(), item.getId(), convocation.getUnitId(), choice, clock.instant(),
                            command.castByUserId()));
        }
        voteRepository.saveAll(votes);

        log.info("Show of hands recorded on agenda item {}: {} lot(s), default {}, {} exception(s)", item.getId(),
                votes.size(), command.defaultChoice(), command.exceptions().size());
        return ballotContext.resultOf(item, convocations);
    }

    private Map<EntityId, Vote> existingVotesByUnit(AgendaItem item) {
        return voteRepository.findByAgendaItemId(item.getId()).stream()
                .collect(java.util.stream.Collectors.toMap(Vote::getUnitId, java.util.function.Function.identity(),
                        (first, second) -> first));
    }
}
