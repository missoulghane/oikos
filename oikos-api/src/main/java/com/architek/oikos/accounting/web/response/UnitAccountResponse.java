package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.accounting.application.dto.UnitAccountView;

public record UnitAccountResponse(String id, String unitId, BigDecimal balance, Instant lastUpdatedDate) {

    public static UnitAccountResponse from(UnitAccountView view) {
        return new UnitAccountResponse(view.id().toString(), view.unitId().toString(), view.balance(),
                view.lastUpdatedDate());
    }
}
