package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.GetMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.query.GetMeetingMinutesQuery;
import com.architek.oikos.meeting.domain.exception.MeetingMinutesNotFoundException;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;

@Component
public class GetMeetingMinutesService implements GetMeetingMinutesUseCase {

    private final MeetingMinutesRepository meetingMinutesRepository;

    public GetMeetingMinutesService(MeetingMinutesRepository meetingMinutesRepository) {
        this.meetingMinutesRepository = meetingMinutesRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingMinutesView getMinutes(GetMeetingMinutesQuery query) {
        return MeetingMinutesView.from(meetingMinutesRepository.findByGeneralMeetingId(query.generalMeetingId())
                .orElseThrow(() -> new MeetingMinutesNotFoundException(query.generalMeetingId())));
    }
}
