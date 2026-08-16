package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;

public interface ListConvocationsByMeetingUseCase {

    List<ConvocationView> listConvocations(ListConvocationsByMeetingQuery query);
}
