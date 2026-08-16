package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.CloseGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.CloseGeneralMeetingUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

/** Ends the session. From here the minutes can be drafted (lot 5). */
@Component
public class CloseGeneralMeetingService implements CloseGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public CloseGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                       GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional
    public GeneralMeetingView close(CloseGeneralMeetingCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.id()));
        return viewAssembler.toView(generalMeetingRepository.save(meeting.close()));
    }
}
