package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * What AllocatePaymentService needs to validate a manual allocation against a
 * movement, without depending on accounting's Movement model directly (rule 4).
 */
public record MovementInfo(EntityId accountId, boolean credit, BigDecimal amount) {
}
