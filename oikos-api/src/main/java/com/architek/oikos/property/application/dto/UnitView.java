package com.architek.oikos.property.application.dto;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public record UnitView(UnitId id, BuildingId buildingId, PropertyId propertyId, String unitNumber,
                       UnitTypeDefinitionId unitTypeId, String unitTypeName, BigDecimal shares,
                       OwnershipStatus ownershipStatus, List<String> ownerFullNames) {

    /**
     * hasCoproprietaires vient de l'appelant (RG-LOT-01): un unit sans aucun
     * UnitOwnership associe est etiquete "Non affecte". unitTypeName vient
     * aussi de l'appelant (resolu via UnitTypeDefinitionRepository), pour
     * eviter un aller-retour supplementaire cote consommateur (Swagger, front).
     */
    public static UnitView from(Unit unit, boolean hasCoproprietaires, String unitTypeName) {
        return from(unit, hasCoproprietaires, unitTypeName, List.of());
    }

    /**
     * ownerFullNames vient de l'appelant, resolu via PartyDirectoryPort, pour
     * les vues liste (ListUnitsByBuildingService) qui affichent le nom des
     * coproprietaires sans aller-retour supplementaire cote front.
     */
    public static UnitView from(Unit unit, boolean hasCoproprietaires, String unitTypeName, List<String> ownerFullNames) {
        OwnershipStatus statut = hasCoproprietaires ? OwnershipStatus.AFFECTED : OwnershipStatus.NOT_AFFECTED;
        return new UnitView(unit.getId(), unit.getBuildingId(), unit.getPropertyId(), unit.getUnitNumber(),
                unit.getUnitTypeId(), unitTypeName, unit.getShares().value(), statut, ownerFullNames);
    }
}
