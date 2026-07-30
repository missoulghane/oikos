package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.PendingLettrageView;

public record PendingLettrageResponse(String unitId, BigDecimal proposedAmount,
                                       BigDecimal remainingUnmatchedDebitAfter,
                                       BigDecimal remainingUnallocatedCreditAfter) {

    public static PendingLettrageResponse from(PendingLettrageView view) {
        return new PendingLettrageResponse(view.unitId().toString(), view.proposedAmount(),
                view.remainingUnmatchedDebitAfter(), view.remainingUnallocatedCreditAfter());
    }
}
