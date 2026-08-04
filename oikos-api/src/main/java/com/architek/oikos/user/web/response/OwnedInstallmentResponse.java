package com.architek.oikos.user.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.user.application.port.out.OwnedInstallmentStatus;
import com.architek.oikos.user.application.port.out.OwnedInstallmentView;

public record OwnedInstallmentResponse(String id, String unitId, LocalDate dueDate, BigDecimal amount,
                                        BigDecimal outstandingAmount, OwnedInstallmentStatus status) {

    public static OwnedInstallmentResponse from(OwnedInstallmentView view) {
        return new OwnedInstallmentResponse(view.id().toString(), view.unitId().toString(), view.dueDate(),
                view.amount(), view.outstandingAmount(), view.status());
    }
}
