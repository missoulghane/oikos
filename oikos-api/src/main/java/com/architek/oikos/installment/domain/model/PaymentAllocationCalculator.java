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

    public static Result allocate(BigDecimal paymentAmount, List<UnsettledInstallment> unsettledInstallments) {
        List<UnsettledInstallment> fifoOrder = unsettledInstallments.stream()
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
