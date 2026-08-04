package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitBasicInfo(EntityId propertyId, String unitNumber, String unitTypeName, boolean available) {
}
