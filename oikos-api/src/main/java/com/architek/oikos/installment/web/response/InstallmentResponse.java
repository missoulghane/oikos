package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

public record InstallmentResponse(String id, String unitId, LocalDate dueDate, BigDecimal amount,
                                      InstallmentStatus status) {

    public static InstallmentResponse from(InstallmentView view) {
        return new InstallmentResponse(view.id().toString(), view.unitId().toString(),
                view.dueDate(), view.amount(), view.status());
    }
}
