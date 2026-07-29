package com.architek.oikos.user.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OwnedUnitView(EntityId unitId, String unitNumber, EntityId buildingId, String buildingName,
                             EntityId propertyId, String propertyName, BigDecimal ownershipShare) {
}
