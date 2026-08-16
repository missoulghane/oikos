package com.architek.oikos.meeting.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.domain.exception.AgendaItemNotFoundException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.MeetingNotInProgressException;
import com.architek.oikos.meeting.domain.exception.UnitNotCheckedInException;
import com.architek.oikos.meeting.domain.exception.UnitNotConvokedException;
import com.architek.oikos.meeting.domain.exception.VoteSessionNotOpenException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.meeting.domain.service.MajorityRuleEvaluator;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.VoteTally;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Loads what a ballot needs - the item, its meeting, the convocations - and
 * holds the three guards every vote has to pass. Shared by the four voting
 * services rather than repeated: guards that exist in four copies are guards
 * that end up differing in one.
 *
 * <p>The guards are separate on purpose, and fail with distinct errors. "The
 * session has not started", "this point is not under discussion" and "that lot
 * is not in the room" are three different things for whoever is chairing.
 */
@Component
public class BallotContext {

    private final AgendaItemRepository agendaItemRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final VoteRepository voteRepository;

    public BallotContext(AgendaItemRepository agendaItemRepository, GeneralMeetingRepository generalMeetingRepository,
                          ConvocationRepository convocationRepository, VoteRepository voteRepository) {
        this.agendaItemRepository = agendaItemRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.voteRepository = voteRepository;
    }

    public AgendaItem requireAgendaItem(AgendaItemId id) {
        return agendaItemRepository.findById(id).orElseThrow(() -> new AgendaItemNotFoundException(id));
    }

    public GeneralMeeting requireMeetingOf(AgendaItem item) {
        return generalMeetingRepository.findById(item.getGeneralMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(item.getGeneralMeetingId()));
    }

    /** Opening a ballot, closing it or voting all require a session under way. */
    public void requireMeetingInProgress(GeneralMeeting meeting) {
        if (meeting.getStatus() != MeetingStatus.IN_PROGRESS) {
            throw new MeetingNotInProgressException(meeting.getStatus());
        }
    }

    public void requireBallotOpen(AgendaItem item) {
        if (!item.acceptsVotes()) {
            throw new VoteSessionNotOpenException(item.getVoteSessionStatus());
        }
    }

    public List<Convocation> convocationsOf(GeneralMeeting meeting) {
        return convocationRepository.findByGeneralMeetingId(meeting.getId());
    }

    public Map<EntityId, Convocation> convocationsByUnit(GeneralMeeting meeting) {
        return convocationsOf(meeting).stream()
                .collect(Collectors.toMap(Convocation::getUnitId, Function.identity(), (first, second) -> first));
    }

    /**
     * A lot may vote only if it was convoked to this meeting and signed in.
     * Presence is the same fact the quorum was computed from - letting a lot
     * vote without it would put weight in the result that never counted
     * towards opening the session.
     */
    public Convocation requireVotingLot(Map<EntityId, Convocation> byUnit, EntityId unitId, String unitLabel) {
        Convocation convocation = byUnit.get(unitId);
        if (convocation == null) {
            throw new UnitNotConvokedException(unitId.toString());
        }
        if (!convocation.isCheckedIn()) {
            throw new UnitNotCheckedInException(unitLabel == null ? unitId.toString() : unitLabel);
        }
        return convocation;
    }

    /** The standing of the vote right now: mid-session it is provisional, after closing it is the result. */
    public AgendaItemResultView resultOf(AgendaItem item, List<Convocation> convocations) {
        VoteTally tally = VoteTally.of(voteRepository.findByAgendaItemId(item.getId()), convocations);
        return AgendaItemResultView.from(item.getId(), item.getLabel(), item.getMajorityRule(),
                item.getVoteSessionStatus(), tally, MajorityRuleEvaluator.evaluate(item.getMajorityRule(), tally));
    }
}
