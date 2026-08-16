package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MeetingQuorumSettingView(EntityId propertyId, MeetingType meetingType, BigDecimal quorumPercentage) {

    public static MeetingQuorumSettingView from(MeetingQuorumSetting setting) {
        return new MeetingQuorumSettingView(setting.getPropertyId(), setting.getMeetingType(),
                setting.getQuorumPercentage().value());
    }
}
