package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitType;

public record AddUnitCommand(BuildingId buildingId, String unitNumber, UnitType unitType, BigDecimal shares) {
}
