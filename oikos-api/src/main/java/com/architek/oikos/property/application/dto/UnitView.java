package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitType;

public record UnitView(UnitId id, BuildingId buildingId, String unitNumber, UnitType unitType, BigDecimal shares,
                       OwnershipStatus ownershipStatus) {

    /**
     * hasCoproprietaires vient de l'appelant (RG-LOT-01): un unit sans aucun
     * UnitOwnership associe est etiquete "Non vendu / Promoteur".
     */
    public static UnitView from(Unit unit, boolean hasCoproprietaires) {
        OwnershipStatus statut = hasCoproprietaires ? OwnershipStatus.SOLD : OwnershipStatus.UNSOLD_DEVELOPER;
        return new UnitView(unit.getId(), unit.getBuildingId(), unit.getUnitNumber(), unit.getUnitType(),
                unit.getShares().value(), statut);
    }
}
