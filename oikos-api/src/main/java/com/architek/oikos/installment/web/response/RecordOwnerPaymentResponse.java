package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.installment.application.dto.RecordOwnerPaymentResult;

public record RecordOwnerPaymentResponse(PaymentResponse payment, List<InstallmentAllocationResponse> allocations,
                                          BigDecimal advanceAmount) {

    public static RecordOwnerPaymentResponse from(RecordOwnerPaymentResult result) {
        return new RecordOwnerPaymentResponse(PaymentResponse.from(result.payment()),
                result.allocations().stream().map(InstallmentAllocationResponse::from).toList(),
                result.advanceAmount());
    }
}
