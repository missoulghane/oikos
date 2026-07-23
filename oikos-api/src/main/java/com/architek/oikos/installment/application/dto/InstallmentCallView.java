package com.architek.oikos.installment.application.dto;

import java.time.LocalDate;
import java.time.YearMonth;

import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentCallView(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {

    public static InstallmentCallView from(InstallmentCall installmentCall) {
        return new InstallmentCallView(installmentCall.getId(), installmentCall.getPropertyId(),
                installmentCall.getPeriod(), installmentCall.getDueDate());
    }
}
