package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public record UnitView(UnitId id, BuildingId buildingId, String unitNumber, UnitTypeDefinitionId unitTypeId,
                       String unitTypeName, BigDecimal shares, OwnershipStatus ownershipStatus) {

    /**
     * hasCoproprietaires vient de l'appelant (RG-LOT-01): un unit sans aucun
     * UnitOwnership associe est etiquete "Non affecte". unitTypeName vient
     * aussi de l'appelant (resolu via UnitTypeDefinitionRepository), pour
     * eviter un aller-retour supplementaire cote consommateur (Swagger, front).
     */
    public static UnitView from(Unit unit, boolean hasCoproprietaires, String unitTypeName) {
        OwnershipStatus statut = hasCoproprietaires ? OwnershipStatus.AFFECTED : OwnershipStatus.NOT_AFFECTED;
        return new UnitView(unit.getId(), unit.getBuildingId(), unit.getUnitNumber(), unit.getUnitTypeId(),
                unitTypeName, unit.getShares().value(), statut);
    }
}
