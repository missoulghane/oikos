package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.model.PaymentAllocationCalculator.InstallmentAllocation;
import com.architek.oikos.installment.domain.model.PaymentAllocationCalculator.Result;
import com.architek.oikos.installment.domain.model.PaymentAllocationCalculator.UnsettledInstallment;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class PaymentAllocationCalculatorTest {

    @Test
    void a_payment_exactly_covering_the_only_unsettled_installment_leaves_no_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("300.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))));

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void golden_dataset_lot04_espece_1200_imputes_300_and_leaves_900_as_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("1200.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))));

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo("900.00");
    }

    @Test
    void golden_dataset_lot02_virement_1000_imputes_300_and_leaves_700_as_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("1000.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))));

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo("700.00");
    }

    @Test
    void I8_allocation_follows_fifo_order_by_due_date_and_never_exceeds_an_installment_s_outstanding_amount() {
        EntityId oldest = EntityId.newId();
        EntityId middle = EntityId.newId();
        EntityId newest = EntityId.newId();

        // Only enough to fully settle the two oldest and partially settle the third.
        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("450.00"), List.of(
                new UnsettledInstallment(newest, LocalDate.of(2026, 10, 1), new BigDecimal("300.00")),
                new UnsettledInstallment(oldest, LocalDate.of(2026, 8, 1), new BigDecimal("300.00")),
                new UnsettledInstallment(middle, LocalDate.of(2026, 9, 1), new BigDecimal("300.00"))));

        assertThat(result.allocations()).containsExactly(
                new InstallmentAllocation(oldest, new BigDecimal("300.00")),
                new InstallmentAllocation(middle, new BigDecimal("150.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void a_payment_with_no_unsettled_installments_is_entirely_an_advance() {
        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("500.00"), List.of());

        assertThat(result.allocations()).isEmpty();
        assertThat(result.advanceAmount()).isEqualByComparingTo("500.00");
    }
}
