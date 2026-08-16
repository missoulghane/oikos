package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OwnedUnitInfo(EntityId unitId, String unitNumber, String buildingName, EntityId propertyId,
                             String propertyName) {
}
