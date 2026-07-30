package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.UnitAccountSummaryView;

public record UnitAccountSummaryResponse(String unitId, BigDecimal totalDue, BigDecimal totalPaid,
                                          BigDecimal availableAdvance, BigDecimal currentBalance) {

    public static UnitAccountSummaryResponse from(UnitAccountSummaryView view) {
        return new UnitAccountSummaryResponse(view.unitId().toString(), view.totalDue(), view.totalPaid(),
                view.availableAdvance(), view.currentBalance());
    }
}
