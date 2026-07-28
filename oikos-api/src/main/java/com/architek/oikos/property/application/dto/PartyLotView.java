package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

public record PartyLotView(UnitOwnershipId id, UnitId unitId, String unitNumber, BuildingId buildingId,
                              String buildingName, PropertyId propertyId, String propertyName,
                              BigDecimal ownershipShare) {

    public static PartyLotView from(UnitOwnership unitOwnership, String unitNumber, BuildingId buildingId,
                                     String buildingName, PropertyId propertyId, String propertyName) {
        return new PartyLotView(unitOwnership.getId(), unitOwnership.getUnitId(), unitNumber, buildingId,
                buildingName, propertyId, propertyName, unitOwnership.getOwnershipShare().value());
    }
}
