package com.architek.oikos.user.web.response;

import java.math.BigDecimal;

import com.architek.oikos.user.application.port.out.OwnedUnitView;

public record OwnedUnitResponse(String unitId, String unitNumber, String buildingId, String buildingName,
                                 String propertyId, String propertyName, BigDecimal ownershipShare) {

    public static OwnedUnitResponse from(OwnedUnitView view) {
        return new OwnedUnitResponse(view.unitId().toString(), view.unitNumber(), view.buildingId().toString(),
                view.buildingName(), view.propertyId().toString(), view.propertyName(), view.ownershipShare());
    }
}
