package com.architek.oikos.party.application.command;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record UpdatePartyCommand(PartyId id, String fullName, PartyType partyType, EmailVO email, String phone) {
}
