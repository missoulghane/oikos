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

    /** Later than every due date below, so these cases exercise the split alone. */
    private static final LocalDate EVERYTHING_DUE = LocalDate.of(2026, 12, 31);

    @Test
    void a_payment_exactly_covering_the_only_unsettled_installment_leaves_no_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("300.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))),
                EVERYTHING_DUE);

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void golden_dataset_lot04_espece_1200_imputes_300_and_leaves_900_as_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("1200.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))),
                EVERYTHING_DUE);

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo("900.00");
    }

    @Test
    void golden_dataset_lot02_virement_1000_imputes_300_and_leaves_700_as_advance() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("1000.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 8, 1), new BigDecimal("300.00"))),
                EVERYTHING_DUE);

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
                new UnsettledInstallment(middle, LocalDate.of(2026, 9, 1), new BigDecimal("300.00"))),
                EVERYTHING_DUE);

        assertThat(result.allocations()).containsExactly(
                new InstallmentAllocation(oldest, new BigDecimal("300.00")),
                new InstallmentAllocation(middle, new BigDecimal("150.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // Spec §4.2 says "appels échus" - a call the owner does not owe yet must not
    // be settled by today's payment, or the arrears it was meant to clear stay
    // open while next year's line reads as paid.
    @Test
    void an_installment_not_yet_fallen_due_is_left_alone_and_the_money_becomes_an_advance() {
        EntityId due = EntityId.newId();
        EntityId notYetDue = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("1000.00"), List.of(
                new UnsettledInstallment(due, LocalDate.of(2026, 8, 1), new BigDecimal("300.00")),
                new UnsettledInstallment(notYetDue, LocalDate.of(2026, 10, 1), new BigDecimal("300.00"))),
                LocalDate.of(2026, 9, 15));

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(due, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo("700.00");
    }

    // The boundary belongs to the payer: an echeance is due on its due date.
    @Test
    void an_installment_falling_due_on_the_very_day_is_imputed() {
        EntityId installmentId = EntityId.newId();

        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("300.00"), List.of(
                new UnsettledInstallment(installmentId, LocalDate.of(2026, 9, 15), new BigDecimal("300.00"))),
                LocalDate.of(2026, 9, 15));

        assertThat(result.allocations()).containsExactly(new InstallmentAllocation(installmentId, new BigDecimal("300.00")));
        assertThat(result.advanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void a_payment_with_no_unsettled_installments_is_entirely_an_advance() {
        Result result = PaymentAllocationCalculator.allocate(new BigDecimal("500.00"), List.of(), EVERYTHING_DUE);

        assertThat(result.allocations()).isEmpty();
        assertThat(result.advanceAmount()).isEqualByComparingTo("500.00");
    }
}
