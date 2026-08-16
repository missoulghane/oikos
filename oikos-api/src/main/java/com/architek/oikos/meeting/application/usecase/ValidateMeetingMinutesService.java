package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ValidateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.ValidateMeetingMinutesUseCase;
import com.architek.oikos.meeting.domain.exception.MeetingMinutesNotFoundException;
import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;

/** Freezes the text. Nothing but publication may follow. */
@Component
public class ValidateMeetingMinutesService implements ValidateMeetingMinutesUseCase {

    private final MeetingMinutesRepository meetingMinutesRepository;

    public ValidateMeetingMinutesService(MeetingMinutesRepository meetingMinutesRepository) {
        this.meetingMinutesRepository = meetingMinutesRepository;
    }

    @Override
    @Transactional
    public MeetingMinutesView validate(ValidateMeetingMinutesCommand command) {
        MeetingMinutes minutes = meetingMinutesRepository.findByGeneralMeetingId(command.generalMeetingId())
                .orElseThrow(() -> new MeetingMinutesNotFoundException(command.generalMeetingId()));
        return MeetingMinutesView.from(meetingMinutesRepository.save(minutes.validate()));
    }
}
