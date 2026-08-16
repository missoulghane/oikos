package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.query.ListGeneralMeetingsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListGeneralMeetingsByPropertyUseCase {

    Page<GeneralMeetingView> listGeneralMeetings(ListGeneralMeetingsByPropertyQuery query);
}
