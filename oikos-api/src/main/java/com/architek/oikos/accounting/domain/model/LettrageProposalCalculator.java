package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;

/**
 * Computes a proposed lettrage (FIFO: oldest fund call first, oldest credit
 * first) - never stored, always recomputed from the movement/allocation
 * history, same "computed not stored" spirit as InstallmentStatusCalculator
 * (installment module). Only FUND_CALL movements are debits to settle here -
 * debit regularizations (penalties, corrections) are final adjustments, not
 * tracked by lettrage. Any credit movement (payment or credit
 * regularization) is eligible as money available to apply.
 */
public final class LettrageProposalCalculator {

    private LettrageProposalCalculator() {
    }

    public static LettrageProposal compute(List<UnitAccountMovement> movements,
                                            List<UnitAccountAllocation> existingAllocations) {
        Map<UnitAccountMovementId, BigDecimal> allocatedPerDebit = sumBy(existingAllocations,
                UnitAccountAllocation::getDebitMovementId);
        Map<UnitAccountMovementId, BigDecimal> allocatedPerCredit = sumBy(existingAllocations,
                UnitAccountAllocation::getCreditMovementId);

        List<Remaining> debits = movements.stream()
                .filter(m -> m.getType() == UnitAccountMovementType.FUND_CALL)
                .map(m -> new Remaining(m, remaining(m, allocatedPerDebit)))
                .filter(r -> r.remaining.signum() > 0)
                .sorted(Comparator.comparing(r -> r.movement.getDate()))
                .toList();

        List<Remaining> credits = movements.stream()
                .filter(m -> m.getDirection() == UnitAccountMovementDirection.CREDIT)
                .map(m -> new Remaining(m, remaining(m, allocatedPerCredit)))
                .filter(r -> r.remaining.signum() > 0)
                .sorted(Comparator.comparing(r -> r.movement.getDate()))
                .toList();

        List<LettrageMovementLine> unsettledDebits = debits.stream().map(Remaining::toLine).toList();
        List<LettrageMovementLine> unallocatedCredits = credits.stream().map(Remaining::toLine).toList();

        List<LettrageAllocationLine> lines = new ArrayList<>();
        int creditIndex = 0;
        for (Remaining debit : debits) {
            while (debit.remaining.signum() > 0 && creditIndex < credits.size()) {
                Remaining credit = credits.get(creditIndex);
                if (credit.remaining.signum() <= 0) {
                    creditIndex++;
                    continue;
                }
                BigDecimal amount = debit.remaining.min(credit.remaining);
                lines.add(new LettrageAllocationLine(debit.movement.getId(), credit.movement.getId(), amount));
                debit.consume(amount);
                credit.consume(amount);
            }
        }

        BigDecimal totalUnmatchedDebit = debits.stream().map(r -> r.remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUnmatchedCredit = credits.stream().map(r -> r.remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LettrageProposal(unsettledDebits, unallocatedCredits, lines, totalUnmatchedDebit,
                totalUnmatchedCredit);
    }

    private static Map<UnitAccountMovementId, BigDecimal> sumBy(List<UnitAccountAllocation> allocations,
                                                                 java.util.function.Function<UnitAccountAllocation, UnitAccountMovementId> keyFn) {
        return allocations.stream()
                .collect(Collectors.groupingBy(keyFn, Collectors.reducing(BigDecimal.ZERO,
                        allocation -> allocation.getAmount().value(), BigDecimal::add)));
    }

    private static BigDecimal remaining(UnitAccountMovement movement, Map<UnitAccountMovementId, BigDecimal> allocated) {
        return movement.getAmount().value().subtract(allocated.getOrDefault(movement.getId(), BigDecimal.ZERO));
    }

    /** Mutable computation-only helper, never exposed outside this calculator. */
    private static final class Remaining {
        private final UnitAccountMovement movement;
        private BigDecimal remaining;

        private Remaining(UnitAccountMovement movement, BigDecimal remaining) {
            this.movement = movement;
            this.remaining = remaining;
        }

        private void consume(BigDecimal amount) {
            remaining = remaining.subtract(amount);
        }

        private LettrageMovementLine toLine() {
            return new LettrageMovementLine(movement.getId(), movement.getDate(), remaining);
        }
    }
}
