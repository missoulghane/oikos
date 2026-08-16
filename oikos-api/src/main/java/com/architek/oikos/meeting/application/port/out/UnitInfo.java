package com.architek.oikos.meeting.application.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** owners is empty for a lot nobody owns yet - convoked all the same, simply unreachable. */
public record UnitInfo(EntityId unitId, String unitNumber, String buildingName, BigDecimal shares,
                        List<OwnerInfo> owners) {
}
