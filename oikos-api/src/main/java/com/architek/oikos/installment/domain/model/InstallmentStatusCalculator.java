package com.architek.oikos.installment.domain.model;

import java.time.LocalDate;

import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

/**
 * Computes an installment's status (never stored) from its due date only.
 */
public final class InstallmentStatusCalculator {

    private InstallmentStatusCalculator() {
    }

    public static InstallmentStatus compute(Installment installment, LocalDate today) {
        if (today.isAfter(installment.getDueDate())) {
            return InstallmentStatus.OVERDUE;
        }
        return InstallmentStatus.NOT_PAID;
    }
}
