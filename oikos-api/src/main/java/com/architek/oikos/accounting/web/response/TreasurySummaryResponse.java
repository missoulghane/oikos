package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.TreasurySummaryView;

public record TreasurySummaryResponse(BigDecimal cashBalance, BigDecimal bankBalance, BigDecimal totalBalance) {

    public static TreasurySummaryResponse from(TreasurySummaryView view) {
        return new TreasurySummaryResponse(view.cashBalance(), view.bankBalance(), view.totalBalance());
    }
}
