package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.dto.UnreadSummaryView;
import com.architek.oikos.messaging.application.query.GetUnreadSummaryQuery;

public interface GetUnreadSummaryUseCase {

    UnreadSummaryView getSummary(GetUnreadSummaryQuery query);
}
