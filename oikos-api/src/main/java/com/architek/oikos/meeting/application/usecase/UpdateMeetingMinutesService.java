package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.UpdateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.UpdateMeetingMinutesUseCase;
import com.architek.oikos.meeting.domain.exception.MeetingMinutesNotFoundException;
import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;

/**
 * The syndic completing the draft: debates, remarks, an opposition a
 * copropriétaire asked to have recorded - everything the figures cannot say.
 * Refused once validated (MinutesLockedException).
 */
@Component
public class UpdateMeetingMinutesService implements UpdateMeetingMinutesUseCase {

    private final MeetingMinutesRepository meetingMinutesRepository;

    public UpdateMeetingMinutesService(MeetingMinutesRepository meetingMinutesRepository) {
        this.meetingMinutesRepository = meetingMinutesRepository;
    }

    @Override
    @Transactional
    public MeetingMinutesView update(UpdateMeetingMinutesCommand command) {
        MeetingMinutes minutes = meetingMinutesRepository.findByGeneralMeetingId(command.generalMeetingId())
                .orElseThrow(() -> new MeetingMinutesNotFoundException(command.generalMeetingId()));
        return MeetingMinutesView.from(meetingMinutesRepository.save(minutes.withContent(command.content())));
    }
}
