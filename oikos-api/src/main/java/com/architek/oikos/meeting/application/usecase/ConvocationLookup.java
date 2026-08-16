package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.ConvocationNotFoundException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;

/**
 * Load-then-view, shared by the four small services that all do exactly that
 * (reply, delivery status, check-in, get). Each of them differs by one line;
 * repeating the lookup, the meeting resolution and the view assembly in four
 * places is how those three end up subtly diverging.
 */
@Component
public class ConvocationLookup {

    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationViewAssembler viewAssembler;

    public ConvocationLookup(ConvocationRepository convocationRepository,
                              GeneralMeetingRepository generalMeetingRepository,
                              ConvocationViewAssembler viewAssembler) {
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    public Convocation require(ConvocationId id) {
        return convocationRepository.findById(id).orElseThrow(() -> new ConvocationNotFoundException(id));
    }

    /** The lot a convocation is addressed to, or null if it no longer exists. */
    public UnitInfo unitOf(Convocation convocation) {
        GeneralMeeting meeting = requireMeetingOf(convocation);
        return viewAssembler.unitsByIdOf(meeting.getPropertyId()).get(convocation.getUnitId());
    }

    public ConvocationView toView(Convocation convocation) {
        return viewAssembler.toView(convocation, unitOf(convocation));
    }

    public ConvocationView save(Convocation convocation) {
        return toView(convocationRepository.save(convocation));
    }

    /**
     * Saves against a lot the caller has already resolved. Worth the overload:
     * resolving it walks the whole copropriété's lots through the property
     * module, and a service that needed the unit to decide what to save would
     * otherwise pay for that walk twice.
     */
    public ConvocationView save(Convocation convocation, UnitInfo unit) {
        return viewAssembler.toView(convocationRepository.save(convocation), unit);
    }

    private GeneralMeeting requireMeetingOf(Convocation convocation) {
        return generalMeetingRepository.findById(convocation.getGeneralMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(convocation.getGeneralMeetingId()));
    }
}
