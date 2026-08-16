package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;

public record AddAgendaItemCommand(GeneralMeetingId generalMeetingId, String label, String description,
                                    MajorityRule majorityRule) {
}
