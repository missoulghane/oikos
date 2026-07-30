package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** One unit whose current proposal has something to validate (spec: the bulk review screen). */
public record PendingLettrageView(EntityId unitId, BigDecimal proposedAmount, BigDecimal remainingUnmatchedDebitAfter,
                                   BigDecimal remainingUnallocatedCreditAfter) {
}
