package com.architek.oikos.party.application.command;

import com.architek.oikos.party.domain.valueobject.PartyId;

public record UpdatePartyPhoneCommand(PartyId id, String phone) {
}
