package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record PropertyContactResponse(String id, String partyId, String partyFullName, PartyType partyType,
                                          String partyEmail, String partyPhone, String unitId, String unitNumber,
                                          String buildingName, BigDecimal ownershipShare, boolean hasLinkedAccount) {

    public static PropertyContactResponse from(PropertyContactView view) {
        return new PropertyContactResponse(view.id().toString(), view.partyId().toString(), view.partyFullName(),
                view.partyType(), view.partyEmail(), view.partyPhone(), view.unitId().toString(), view.unitNumber(),
                view.buildingName(), view.ownershipShare(), view.hasLinkedAccount());
    }
}
