package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.UnitOwnershipView;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record UnitOwnershipResponse(String id, String unitId, String partyId, String partyFullName,
                                        PartyType partyType, String partyEmail, BigDecimal ownershipShare) {

    public static UnitOwnershipResponse from(UnitOwnershipView view) {
        return new UnitOwnershipResponse(view.id().toString(), view.unitId().toString(), view.partyId().toString(),
                view.partyFullName(), view.partyType(), view.partyEmail(), view.ownershipShare());
    }
}
