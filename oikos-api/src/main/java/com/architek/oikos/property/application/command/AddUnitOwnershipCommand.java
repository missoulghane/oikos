package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AddUnitOwnershipCommand(UnitId unitId, EntityId contactId, BigDecimal ownershipShare) {
}
