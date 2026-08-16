package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;

public record OpenVoteSessionCommand(AgendaItemId agendaItemId) {
}
