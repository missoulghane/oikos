package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Spec &sect;4.2: splits an owner payment into the portion imputed to the
 * unit's unsettled fund calls (FIFO by due date - oldest first) and the
 * portion left over as an advance:
 * <pre>
 * part_impute = min(montant_recu, total des appels echus non soldes du lot)
 * part_avance = montant_recu - part_impute
 * </pre>
 * "Echus" is enforced here rather than left to each caller: an installment
 * whose due date has not been reached at {@code asOf} is never imputed, and
 * the money that would have gone to it stays an advance - to be taken up by
 * the regularization sweep once it does fall due. Without that cutoff a
 * payment silently settles next year's call, which then shows as a settled
 * line the owner has no reason to see and leaves the arrears it was meant to
 * clear untouched.
 * <p>
 * I8 (never allocated beyond an installment's remaining due) holds by
 * construction: each allocation is capped at min(remaining payment,
 * that installment's own outstanding amount). Pure and stateless, like
 * InstallmentStatusCalculator.
 */
public final class PaymentAllocationCalculator {

    private PaymentAllocationCalculator() {
    }

    public record UnsettledInstallment(EntityId installmentId, LocalDate dueDate, BigDecimal outstandingAmount) {
    }

    public record InstallmentAllocation(EntityId installmentId, BigDecimal amount) {
    }

    public record Result(List<InstallmentAllocation> allocations, BigDecimal advanceAmount) {
    }

    /**
     * @param asOf the date the money is considered received on - a payment's
     *             value date, or the piece date of a regularization. Anything
     *             falling due after it is left out of the imputation.
     */
    public static Result allocate(BigDecimal paymentAmount, List<UnsettledInstallment> unsettledInstallments,
                                   LocalDate asOf) {
        List<UnsettledInstallment> fifoOrder = unsettledInstallments.stream()
                .filter(installment -> !installment.dueDate().isAfter(asOf))
                .sorted(Comparator.comparing(UnsettledInstallment::dueDate)
                        .thenComparing(installment -> installment.installmentId().value()))
                .toList();

        BigDecimal remaining = paymentAmount;
        List<InstallmentAllocation> allocations = new ArrayList<>();
        for (UnsettledInstallment installment : fifoOrder) {
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal amountToAllocate = remaining.min(installment.outstandingAmount());
            if (amountToAllocate.signum() > 0) {
                allocations.add(new InstallmentAllocation(installment.installmentId(), amountToAllocate));
                remaining = remaining.subtract(amountToAllocate);
            }
        }
        return new Result(allocations, remaining);
    }
}
