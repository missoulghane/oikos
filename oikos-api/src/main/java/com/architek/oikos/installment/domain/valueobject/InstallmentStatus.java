package com.architek.oikos.installment.domain.valueobject;

/**
 * Never persisted: always computed at read time from dueDate vs the current
 * date.
 */
public enum InstallmentStatus {
    NOT_PAID,
    OVERDUE
}
