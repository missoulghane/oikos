package com.architek.oikos.installment.domain.valueobject;

/**
 * Derived from amount vs outstandingAmount (kept in sync by accounting via
 * lettrage validation) - never itself persisted.
 */
public enum InstallmentStatus {
    NOT_SETTLED,
    PARTIALLY_SETTLED,
    SETTLED
}
