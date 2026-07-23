package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;

import com.architek.oikos.installment.application.dto.AllocationView;

public record AllocationResponse(String id, String movementId, String installmentId, BigDecimal allocatedAmount) {

    public static AllocationResponse from(AllocationView view) {
        return new AllocationResponse(view.id().toString(), view.movementId().toString(), view.installmentId().toString(),
                view.allocatedAmount());
    }
}
