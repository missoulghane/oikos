package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentView(InstallmentId id, EntityId unitId, LocalDate dueDate,
                                  BigDecimal amount, BigDecimal outstandingAmount, InstallmentStatus status) {
}
