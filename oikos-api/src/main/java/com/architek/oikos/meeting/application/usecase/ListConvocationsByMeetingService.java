package com.architek.oikos.meeting.application.usecase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The tracking table of the SFD: sent / confirmed / present, lot by lot.
 *
 * <p>Unpaged, and filtered in memory on the derived status. The set is one row
 * per lot of a single copropriété, it is read as a whole (the syndic scans it
 * for the lots still silent), and the status it filters on is computed rather
 * than stored - so no database predicate could express it anyway.
 */
@Component
public class ListConvocationsByMeetingService implements ListConvocationsByMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final ConvocationViewAssembler viewAssembler;

    public ListConvocationsByMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                             ConvocationRepository convocationRepository,
                                             ConvocationViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocationView> listConvocations(ListConvocationsByMeetingQuery query) {
        GeneralMeeting meeting = generalMeetingRepository.findById(query.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(query.generalMeetingId()));

        Map<EntityId, UnitInfo> unitsById = viewAssembler.unitsByIdOf(meeting.getPropertyId());
        List<ConvocationView> views = viewAssembler
                .toViews(convocationRepository.findByGeneralMeetingId(meeting.getId()), unitsById);

        if (query.status() == null) {
            return views;
        }
        return views.stream().filter(view -> view.status() == query.status()).toList();
    }
}
