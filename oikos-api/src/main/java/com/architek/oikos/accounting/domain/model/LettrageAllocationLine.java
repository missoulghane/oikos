package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;

/** One proposed match: this much of the credit movement would settle this much of the debit movement. */
public record LettrageAllocationLine(UnitAccountMovementId debitMovementId, UnitAccountMovementId creditMovementId,
                                      BigDecimal amount) {
}
