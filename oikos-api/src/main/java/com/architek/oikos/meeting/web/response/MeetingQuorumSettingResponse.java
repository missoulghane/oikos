package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.meeting.application.dto.MeetingQuorumSettingView;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;

public record MeetingQuorumSettingResponse(String propertyId, MeetingType meetingType, BigDecimal quorumPercentage) {

    public static MeetingQuorumSettingResponse from(MeetingQuorumSettingView view) {
        return new MeetingQuorumSettingResponse(view.propertyId().toString(), view.meetingType(),
                view.quorumPercentage());
    }
}
