package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.UnitView;

public record UnitResponse(String id, String buildingId, String unitNumber, String unitType, BigDecimal shares,
                           String ownershipStatus) {

    public static UnitResponse from(UnitView view) {
        return new UnitResponse(view.id().toString(), view.buildingId().toString(), view.unitNumber(),
                view.unitType().name(), view.shares(), view.ownershipStatus().name());
    }
}
