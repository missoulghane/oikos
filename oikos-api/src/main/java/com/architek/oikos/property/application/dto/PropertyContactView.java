package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

public record PropertyContactView(UnitOwnershipId id, EntityId partyId, String partyFullName, PartyType partyType,
                                      String partyEmail, String partyPhone, UnitId unitId, String unitNumber,
                                      String buildingName, BigDecimal ownershipShare, boolean hasLinkedAccount) {

    public static PropertyContactView from(UnitOwnership unitOwnership, PartyDetails partyDetails,
                                            String unitNumber, String buildingName, boolean hasLinkedAccount) {
        return new PropertyContactView(unitOwnership.getId(), unitOwnership.getPartyId(), partyDetails.fullName(),
                partyDetails.partyType(),
                partyDetails.email() != null ? partyDetails.email().value() : null,
                partyDetails.phone(), unitOwnership.getUnitId(),
                unitNumber, buildingName, unitOwnership.getOwnershipShare().value(), hasLinkedAccount);
    }
}
