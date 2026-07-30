package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;

/** A movement paired with its currently outstanding (debit) or available (credit) amount. */
public record LettrageMovementView(UnitAccountMovementView movement, BigDecimal remainingAmount) {

    public static LettrageMovementView of(UnitAccountMovement movement, BigDecimal remainingAmount) {
        return new LettrageMovementView(UnitAccountMovementView.from(movement), remainingAmount);
    }
}
