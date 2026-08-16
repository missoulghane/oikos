package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.AttendanceSummaryView;
import com.architek.oikos.meeting.application.port.in.GetAttendanceSummaryUseCase;
import com.architek.oikos.meeting.application.query.GetAttendanceSummaryQuery;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AttendanceTally;

/**
 * Recomputed on every read from the convocations themselves - a stored count
 * is a count that can disagree with its own rows, and this one decides whether
 * a session may open.
 *
 * <p>The quorum is compared against the meeting's own snapshot, not against
 * the property's current setting: changing the threshold today must not
 * retroactively make last year's meeting inquorate.
 */
@Component
public class GetAttendanceSummaryService implements GetAttendanceSummaryUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;

    public GetAttendanceSummaryService(GeneralMeetingRepository generalMeetingRepository,
                                        ConvocationRepository convocationRepository) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryView getSummary(GetAttendanceSummaryQuery query) {
        GeneralMeeting meeting = generalMeetingRepository.findById(query.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(query.generalMeetingId()));
        AttendanceTally tally = AttendanceTally.of(convocationRepository.findByGeneralMeetingId(meeting.getId()));
        return AttendanceSummaryView.from(tally, meeting.getQuorumPercentage());
    }
}
