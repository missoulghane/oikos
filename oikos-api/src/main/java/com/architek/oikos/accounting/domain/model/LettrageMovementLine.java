package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;

/** A movement's currently outstanding (debit) or available (credit) amount, before any proposal is applied. */
public record LettrageMovementLine(UnitAccountMovementId movementId, LocalDate date, BigDecimal amount) {
}
