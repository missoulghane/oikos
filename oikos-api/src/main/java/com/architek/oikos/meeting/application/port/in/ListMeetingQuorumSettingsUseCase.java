package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.dto.MeetingQuorumSettingView;
import com.architek.oikos.meeting.application.query.ListMeetingQuorumSettingsQuery;

public interface ListMeetingQuorumSettingsUseCase {

    List<MeetingQuorumSettingView> listSettings(ListMeetingQuorumSettingsQuery query);
}
