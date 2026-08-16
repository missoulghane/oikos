package com.architek.oikos.meeting.application.query;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** status and meetingType are optional filters; null means no filter on that criterion. */
public record ListGeneralMeetingsByPropertyQuery(EntityId propertyId, MeetingStatus status, MeetingType meetingType,
                                                  PageRequest pageRequest) {
}
