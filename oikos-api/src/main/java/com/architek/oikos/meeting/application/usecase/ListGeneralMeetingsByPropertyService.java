package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.ListGeneralMeetingsByPropertyUseCase;
import com.architek.oikos.meeting.application.query.ListGeneralMeetingsByPropertyQuery;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.shared.domain.pagination.Page;

/**
 * Paged in the database rather than in memory (unlike ListMyConversationsService,
 * whose list has to be aggregated across modules first): a copropriété
 * accumulates meetings year after year and nothing here needs the whole
 * history in memory to answer for one page of it.
 */
@Component
public class ListGeneralMeetingsByPropertyService implements ListGeneralMeetingsByPropertyUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public ListGeneralMeetingsByPropertyService(GeneralMeetingRepository generalMeetingRepository,
                                                 GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeneralMeetingView> listGeneralMeetings(ListGeneralMeetingsByPropertyQuery query) {
        Page<GeneralMeeting> page = generalMeetingRepository.findByProperty(query.propertyId(), query.status(),
                query.meetingType(), query.pageRequest());
        return Page.of(viewAssembler.toViews(page.content()), page.pageNumber(), page.pageSize(), page.totalElements());
    }
}
