package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.AttendanceSummaryView;
import com.architek.oikos.meeting.application.query.GetAttendanceSummaryQuery;

public interface GetAttendanceSummaryUseCase {

    AttendanceSummaryView getSummary(GetAttendanceSummaryQuery query);
}
