package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class InstallmentStatusCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    private static Installment installmentDueOn(LocalDate dueDate) {
        return Installment.create(InstallmentId.newId(), EntityId.newId(), EntityId.newId(), dueDate,
                Amount.of(new BigDecimal("250")));
    }

    @Test
    void no_payment_and_not_yet_due_is_not_paid() {
        Installment installment = installmentDueOn(TODAY.plusDays(10));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, BigDecimal.ZERO, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.NOT_PAID);
    }

    @Test
    void partial_payment_before_due_date_is_partially_paid() {
        Installment installment = installmentDueOn(TODAY.plusDays(10));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, new BigDecimal("100"), TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.PARTIALLY_PAID);
    }

    @Test
    void fully_paid_is_paid_even_when_overdue() {
        Installment installment = installmentDueOn(TODAY.minusDays(10));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, new BigDecimal("250"), TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.PAID);
    }

    @Test
    void unpaid_and_overdue_is_overdue() {
        Installment installment = installmentDueOn(TODAY.minusDays(1));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, BigDecimal.ZERO, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.OVERDUE);
    }

    @Test
    void partially_paid_and_overdue_is_overdue_not_partially_paid() {
        Installment installment = installmentDueOn(TODAY.minusDays(1));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, new BigDecimal("100"), TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.OVERDUE);
    }

    @Test
    void due_today_is_not_yet_overdue() {
        Installment installment = installmentDueOn(TODAY);
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, BigDecimal.ZERO, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.NOT_PAID);
    }
}
