package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record UnitOwnershipView(UnitOwnershipId id, UnitId unitId, EntityId partyId, String partyFullName,
                                    PartyType partyType, String partyEmail, BigDecimal ownershipShare) {

    public static UnitOwnershipView from(UnitOwnership unitOwnership, PartyDetails partyDetails) {
        return new UnitOwnershipView(unitOwnership.getId(), unitOwnership.getUnitId(), unitOwnership.getPartyId(),
                partyDetails.fullName(), partyDetails.partyType(),
                partyDetails.email() != null ? partyDetails.email().value() : null,
                unitOwnership.getOwnershipShare().value());
    }
}
