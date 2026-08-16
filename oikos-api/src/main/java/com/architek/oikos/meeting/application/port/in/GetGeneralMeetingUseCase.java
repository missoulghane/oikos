package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;

/**
 * Also the hook PropertyAccessEvaluator uses to resolve a meeting id back to
 * its property before authorizing - which is why authorization never needs a
 * repository of this module (rule 6).
 */
public interface GetGeneralMeetingUseCase {

    GeneralMeetingView getGeneralMeeting(GetGeneralMeetingQuery query);
}
