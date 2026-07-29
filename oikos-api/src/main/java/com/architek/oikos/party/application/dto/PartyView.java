package com.architek.oikos.party.application.dto;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record PartyView(PartyId id, EntityId propertyId, String fullName, PartyType partyType, String email,
                         String phone) {

    public static PartyView from(Party party) {
        return new PartyView(party.getId(), party.getPropertyId(), party.getFullName(), party.getPartyType(),
                party.getEmail().value(), party.getPhone());
    }
}
