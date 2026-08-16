package com.architek.oikos.meeting.application.query;

import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;

/** status is an optional filter on the derived statut_global; null means no filter. */
public record ListConvocationsByMeetingQuery(GeneralMeetingId generalMeetingId, ConvocationStatus status) {
}
