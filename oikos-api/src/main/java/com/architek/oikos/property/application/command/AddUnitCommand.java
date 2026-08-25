package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/** floor est facultatif : null vaut "etage inconnu" (voir Unit). */
public record AddUnitCommand(BuildingId buildingId, String unitNumber, UnitTypeDefinitionId unitTypeId,
                             BigDecimal shares, Integer floor) {
}
