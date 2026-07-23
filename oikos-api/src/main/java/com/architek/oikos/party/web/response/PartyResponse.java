package com.architek.oikos.party.web.response;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record PartyResponse(String id, String fullName, PartyType partyType, String email, String phone) {

    public static PartyResponse from(PartyView view) {
        return new PartyResponse(view.id().toString(), view.fullName(), view.partyType(), view.email(), view.phone());
    }
}
