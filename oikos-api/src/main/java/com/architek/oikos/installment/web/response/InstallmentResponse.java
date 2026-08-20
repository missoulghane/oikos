package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

public record InstallmentResponse(String id, String unitId, String unitNumber, LocalDate dueDate, BigDecimal amount,
                                      BigDecimal outstandingAmount, InstallmentStatus status, String period) {

    public static InstallmentResponse from(InstallmentView view) {
        return new InstallmentResponse(view.id().toString(), view.unitId().toString(), view.unitNumber(),
                view.dueDate(), view.amount(), view.outstandingAmount(), view.status(),
                view.period() != null ? view.period().toString() : null);
    }
}
