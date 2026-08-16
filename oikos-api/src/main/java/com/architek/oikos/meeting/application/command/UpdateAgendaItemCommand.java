package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;

public record UpdateAgendaItemCommand(AgendaItemId id, String label, String description, MajorityRule majorityRule) {
}
