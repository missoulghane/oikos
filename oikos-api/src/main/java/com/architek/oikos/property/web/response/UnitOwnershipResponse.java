package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.UnitOwnershipView;

public record UnitOwnershipResponse(String id, String unitId, String contactId, BigDecimal ownershipShare) {

    public static UnitOwnershipResponse from(UnitOwnershipView view) {
        return new UnitOwnershipResponse(view.id().toString(), view.unitId().toString(), view.contactId().toString(),
                view.ownershipShare());
    }
}
