package com.architek.oikos.installment.domain.valueobject;

/**
 * Never persisted (RG011): always computed at read time from an installment's
 * allocations and, for OVERDUE, the current date.
 */
public enum InstallmentStatus {
    NOT_PAID,
    PARTIALLY_PAID,
    PAID,
    OVERDUE
}
