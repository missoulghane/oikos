package com.architek.oikos.property.web.response;

import java.math.BigDecimal;

import com.architek.oikos.property.application.dto.PartyLotView;

public record PartyLotResponse(String id, String unitId, String unitNumber, String buildingId, String buildingName,
                                   String propertyId, String propertyName, BigDecimal ownershipShare) {

    public static PartyLotResponse from(PartyLotView view) {
        return new PartyLotResponse(view.id().toString(), view.unitId().toString(), view.unitNumber(),
                view.buildingId().toString(), view.buildingName(), view.propertyId().toString(), view.propertyName(),
                view.ownershipShare());
    }
}
