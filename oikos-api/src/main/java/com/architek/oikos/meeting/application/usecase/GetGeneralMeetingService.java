package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

@Component
public class GetGeneralMeetingService implements GetGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public GetGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                     GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralMeetingView getGeneralMeeting(GetGeneralMeetingQuery query) {
        return viewAssembler.toView(generalMeetingRepository.findById(query.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(query.id())));
    }
}
