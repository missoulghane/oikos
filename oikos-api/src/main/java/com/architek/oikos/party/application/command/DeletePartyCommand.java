package com.architek.oikos.party.application.command;

import com.architek.oikos.party.domain.valueobject.PartyId;

public record DeletePartyCommand(PartyId id) {
}
