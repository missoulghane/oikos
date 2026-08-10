package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** One FIFO allocation line of a P3 imputation (PaymentAllocationCalculator.InstallmentAllocation, view-mapped). */
public record InstallmentAllocationView(EntityId installmentId, BigDecimal amount) {
}
