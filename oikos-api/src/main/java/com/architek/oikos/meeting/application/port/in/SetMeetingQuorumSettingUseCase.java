package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.SetMeetingQuorumSettingCommand;
import com.architek.oikos.meeting.application.dto.MeetingQuorumSettingView;

public interface SetMeetingQuorumSettingUseCase {

    MeetingQuorumSettingView set(SetMeetingQuorumSettingCommand command);
}
