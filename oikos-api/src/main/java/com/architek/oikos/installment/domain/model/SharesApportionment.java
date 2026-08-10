package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * I9 (spec &sect;5): splits a total amount across units in proportion to
 * their shares (tantiemes) using the largest-remainder method - exact shares
 * computed in cents, truncated, then the leftover cents distributed one by
 * one to the units with the largest truncated fraction, ties broken by unit
 * id ascending order for a deterministic result. Replaces the previous
 * "last unit absorbs the rounding remainder" approach (which reconciled the
 * total but distributed the rounding unfairly onto a single, arbitrary
 * unit). Pure and stateless, like InstallmentStatusCalculator.
 *
 * <p>I10 (sum of a repartition key's shares equals its declared total,
 * typically 1000 or 10000) has no separate "declared total" stored anywhere
 * in oikos today (Unit.shares has no companion "key total" field) - it is
 * satisfied by construction here, since the denominator is always the live
 * sum of the shares passed in, not a separately-declared figure to reconcile
 * against.
 */
public final class SharesApportionment {

    private SharesApportionment() {
    }

    public record Share(EntityId unitId, BigDecimal shares) {
    }

    public record Allocation(EntityId unitId, BigDecimal amount) {
    }

    public static List<Allocation> apportion(BigDecimal totalAmount, List<Share> shares) {
        if (shares.isEmpty()) {
            throw new IllegalArgumentException("shares must not be empty");
        }
        BigDecimal totalShares = shares.stream().map(Share::shares).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalShares.signum() <= 0) {
            throw new IllegalArgumentException("total shares must be positive");
        }

        long totalCents = totalAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        List<Share> ordered = shares.stream()
                .sorted(Comparator.comparing(share -> share.unitId().value()))
                .toList();

        record Interim(EntityId unitId, long flooredCents, BigDecimal remainder) {
        }

        List<Interim> interim = new ArrayList<>();
        long allocatedCents = 0;
        for (Share share : ordered) {
            BigDecimal exactCents = BigDecimal.valueOf(totalCents).multiply(share.shares())
                    .divide(totalShares, 10, RoundingMode.HALF_UP);
            long floored = exactCents.setScale(0, RoundingMode.DOWN).longValueExact();
            BigDecimal remainder = exactCents.subtract(BigDecimal.valueOf(floored));
            interim.add(new Interim(share.unitId(), floored, remainder));
            allocatedCents += floored;
        }

        long remainingCents = totalCents - allocatedCents;
        List<Interim> byRemainderDesc = interim.stream()
                .sorted(Comparator.comparing(Interim::remainder).reversed())
                .toList();

        Map<EntityId, Long> finalCents = new LinkedHashMap<>();
        interim.forEach(i -> finalCents.put(i.unitId(), i.flooredCents()));
        for (int i = 0; i < remainingCents; i++) {
            finalCents.merge(byRemainderDesc.get(i).unitId(), 1L, Long::sum);
        }

        return ordered.stream()
                .map(share -> new Allocation(share.unitId(),
                        BigDecimal.valueOf(finalCents.get(share.unitId())).movePointLeft(2)))
                .toList();
    }
}
