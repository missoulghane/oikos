package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

/**
 * Computes an installment's status (RG011: never stored). OVERDUE takes
 * priority over PARTIALLY_PAID when both conditions hold (an unsettled,
 * overdue installment is always reported as OVERDUE, whether or not part of
 * it has already been paid) - that is the most actionable signal for a
 * property manager chasing late payments.
 */
public final class InstallmentStatusCalculator {

    private InstallmentStatusCalculator() {
    }

    public static InstallmentStatus compute(Installment installment, BigDecimal amountPaid, LocalDate today) {
        BigDecimal remainingDue = installment.getAmount().value().subtract(amountPaid);
        boolean fullyPaid = remainingDue.signum() <= 0;

        if (fullyPaid) {
            return InstallmentStatus.PAID;
        }
        if (today.isAfter(installment.getDueDate())) {
            return InstallmentStatus.OVERDUE;
        }
        if (amountPaid.signum() > 0) {
            return InstallmentStatus.PARTIALLY_PAID;
        }
        return InstallmentStatus.NOT_PAID;
    }
}
