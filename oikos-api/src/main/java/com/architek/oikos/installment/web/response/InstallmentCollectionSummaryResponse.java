package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.valueobject.InstallmentCollectionSummary;

public record InstallmentCollectionSummaryResponse(long count, BigDecimal amount) {

    public static InstallmentCollectionSummaryResponse from(InstallmentCollectionSummary summary) {
        return new InstallmentCollectionSummaryResponse(summary.count(), summary.amount());
    }
}
