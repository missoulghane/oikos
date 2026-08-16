package com.architek.oikos.meeting.application.usecase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.GenerateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.GenerateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.MeetingNotClosedException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;
import com.architek.oikos.meeting.domain.valueobject.MeetingMinutesId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Drafts the minutes from the session's own data - the moment the module's
 * derived figures become text.
 *
 * <p>Only from a closed session: produced earlier they would record a state
 * still moving, and nothing would refresh them afterwards.
 *
 * <p>Re-running regenerates the draft and discards manual edits. That is the
 * intended behaviour and it is why regenerating is a distinct operation on the
 * aggregate: a syndic who corrected a ballot and wants the figures redone asks
 * for exactly this, and one who has written up the debates must not lose them
 * by accident - which is what validation is for. Regenerating validated
 * minutes is refused.
 */
@Component
public class GenerateMeetingMinutesService implements GenerateMeetingMinutesUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final MeetingMinutesRepository meetingMinutesRepository;
    private final ConvocationRepository convocationRepository;
    private final AgendaItemRepository agendaItemRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler convocationViewAssembler;
    private final BallotContext ballotContext;
    private final MinutesComposer minutesComposer;

    public GenerateMeetingMinutesService(GeneralMeetingRepository generalMeetingRepository,
                                          MeetingMinutesRepository meetingMinutesRepository,
                                          ConvocationRepository convocationRepository,
                                          AgendaItemRepository agendaItemRepository,
                                          PropertyDirectoryPort propertyDirectoryPort,
                                          ConvocationViewAssembler convocationViewAssembler,
                                          BallotContext ballotContext, MinutesComposer minutesComposer) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.meetingMinutesRepository = meetingMinutesRepository;
        this.convocationRepository = convocationRepository;
        this.agendaItemRepository = agendaItemRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.convocationViewAssembler = convocationViewAssembler;
        this.ballotContext = ballotContext;
        this.minutesComposer = minutesComposer;
    }

    @Override
    @Transactional
    public MeetingMinutesView generate(GenerateMeetingMinutesCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));
        if (meeting.getStatus() != MeetingStatus.CLOSED && meeting.getStatus() != MeetingStatus.MINUTES_PUBLISHED) {
            throw new MeetingNotClosedException(meeting.getStatus());
        }

        List<Convocation> convocations = convocationRepository.findByGeneralMeetingId(meeting.getId());
        Map<EntityId, UnitInfo> unitsById = convocationViewAssembler.unitsByIdOf(meeting.getPropertyId());
        List<AgendaItemResultView> results = resultsOf(meeting, convocations);
        String content = minutesComposer.compose(meeting, propertyDirectoryPort.getProperty(meeting.getPropertyId()).name(),
                convocations, unitsById, results);

        MeetingMinutes minutes = meetingMinutesRepository.findByGeneralMeetingId(meeting.getId())
                .map(existing -> existing.regeneratedWith(content))
                .orElseGet(() -> MeetingMinutes.draft(MeetingMinutesId.newId(), meeting.getId(), content));

        return MeetingMinutesView.from(meetingMinutesRepository.save(minutes));
    }

    /** The agenda in reading order, each with the tally computed one last time before it is frozen. */
    private List<AgendaItemResultView> resultsOf(GeneralMeeting meeting, List<Convocation> convocations) {
        return agendaItemRepository.findByGeneralMeetingId(meeting.getId()).stream()
                .map((AgendaItem item) -> ballotContext.resultOf(item, convocations)).toList();
    }
}
