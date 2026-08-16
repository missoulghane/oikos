package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record SetMeetingQuorumSettingCommand(EntityId propertyId, MeetingType meetingType,
                                              QuorumPercentage quorumPercentage) {
}
