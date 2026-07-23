package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.InstallmentCallSummaryView;

public record InstallmentCallSummaryResponse(String id, String propertyId, String period, LocalDate dueDate,
                                                int unitCount, BigDecimal totalAmount) {

    public static InstallmentCallSummaryResponse from(InstallmentCallSummaryView view) {
        return new InstallmentCallSummaryResponse(view.id().toString(), view.propertyId().toString(),
                view.period().toString(), view.dueDate(), view.unitCount(), view.totalAmount());
    }
}
