package com.architek.oikos.party.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

public record CreatePartyCommand(EntityId propertyId, String fullName, PartyType partyType, EmailVO email, String phone) {
}
