package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

/**
 * Computes an installment's status (never stored) from its amount vs
 * outstandingAmount only.
 */
public final class InstallmentStatusCalculator {

    private InstallmentStatusCalculator() {
    }

    public static InstallmentStatus compute(BigDecimal amount, BigDecimal outstandingAmount) {
        if (outstandingAmount.signum() == 0) {
            return InstallmentStatus.SETTLED;
        }
        if (outstandingAmount.compareTo(amount) >= 0) {
            return InstallmentStatus.NOT_SETTLED;
        }
        return InstallmentStatus.PARTIALLY_SETTLED;
    }
}
