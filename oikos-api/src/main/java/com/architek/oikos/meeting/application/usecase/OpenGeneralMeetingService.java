package com.architek.oikos.meeting.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AttendanceTally;

/**
 * Opens the session, once the sign-in sheet says whether the quorum is there.
 *
 * <p>The quorum is evaluated from the lots actually signed in, against the
 * meeting's own snapshot of the threshold. Refusing to open without it is the
 * default; forcing is possible and is recorded on the meeting, because opening
 * anyway is a lawful decision with consequences and the minutes have to say it
 * was taken (ADR 0002 §5).
 */
@Component
public class OpenGeneralMeetingService implements OpenGeneralMeetingUseCase {

    private static final Logger log = LoggerFactory.getLogger(OpenGeneralMeetingService.class);

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public OpenGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                      ConvocationRepository convocationRepository,
                                      GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional
    public GeneralMeetingView open(OpenGeneralMeetingCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.id()));

        AttendanceTally tally = AttendanceTally.of(convocationRepository.findByGeneralMeetingId(meeting.getId()));
        boolean quorumReached = tally.isQuorumReachedFor(meeting.getQuorumPercentage());

        GeneralMeeting opened = meeting.open(quorumReached, command.forceWithoutQuorum());
        if (!quorumReached) {
            log.warn("General meeting {} opened without quorum ({} of {} voting weight present, {}% required)",
                    meeting.getId(), tally.presentWeight().value(), tally.totalWeight().value(),
                    meeting.getQuorumPercentage().value());
        }
        return viewAssembler.toView(generalMeetingRepository.save(opened));
    }
}
