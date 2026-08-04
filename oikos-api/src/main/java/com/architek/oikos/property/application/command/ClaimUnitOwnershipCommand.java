package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ClaimUnitOwnershipCommand(UnitId unitId, EntityId partyId) {
}
