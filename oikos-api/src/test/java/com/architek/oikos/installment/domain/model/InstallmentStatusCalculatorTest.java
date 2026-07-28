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
        return Installment.create(InstallmentId.newId(), EntityId.newId(), dueDate,
                Amount.of(new BigDecimal("250")));
    }

    @Test
    void not_yet_due_is_not_paid() {
        Installment installment = installmentDueOn(TODAY.plusDays(10));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.NOT_PAID);
    }

    @Test
    void past_its_due_date_is_overdue() {
        Installment installment = installmentDueOn(TODAY.minusDays(1));
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.OVERDUE);
    }

    @Test
    void due_today_is_not_yet_overdue() {
        Installment installment = installmentDueOn(TODAY);
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, TODAY);
        assertThat(status).isEqualTo(InstallmentStatus.NOT_PAID);
    }
}
