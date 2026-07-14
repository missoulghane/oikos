package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitOwnershipView(UnitOwnershipId id, UnitId unitId, EntityId contactId, BigDecimal ownershipShare) {

    public static UnitOwnershipView from(UnitOwnership unitOwnership) {
        return new UnitOwnershipView(unitOwnership.getId(), unitOwnership.getUnitId(), unitOwnership.getContactId(),
                unitOwnership.getOwnershipShare().value());
    }
}
