package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AvailableUnitInfo(EntityId id, String unitNumber, String unitTypeName) {
}
