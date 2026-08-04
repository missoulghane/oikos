package com.architek.oikos.invitation.web.response;

import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;

public record AvailableUnitResponse(String id, String unitNumber, String unitTypeName) {

    public static AvailableUnitResponse from(AvailableUnitInfo info) {
        return new AvailableUnitResponse(info.id().toString(), info.unitNumber(), info.unitTypeName());
    }
}
