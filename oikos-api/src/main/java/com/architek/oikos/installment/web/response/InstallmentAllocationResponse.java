package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;

import com.architek.oikos.installment.application.dto.InstallmentAllocationView;

public record InstallmentAllocationResponse(String installmentId, BigDecimal amount) {

    public static InstallmentAllocationResponse from(InstallmentAllocationView view) {
        return new InstallmentAllocationResponse(view.installmentId().toString(), view.amount());
    }
}
