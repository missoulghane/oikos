package com.architek.oikos.accounting.domain.valueobject;

/**
 * Business nature of a movement. The direction (see {@link MovementDirection})
 * is stored separately rather than derived from this type, so that a future
 * type can be added without touching any sign-derivation logic.
 */
public enum MovementType {
    // Credit-side
    PAYMENT,
    INITIAL_BALANCE,
    REFUND,
    CREDIT_NOTE,
    POSITIVE_ADJUSTMENT,
    // Debit-side
    INSTALLMENT,
    PENALTY,
    REMINDER_FEE,
    NEGATIVE_ADJUSTMENT
}
