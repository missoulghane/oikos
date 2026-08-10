package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.valueobject.UnitPositionStatus;

/**
 * Spec &sect;4.3: computes a unit's net position status from
 * (creance - avance), never stored - same computed-not-stored spirit as
 * InstallmentStatusCalculator.
 */
public final class UnitPositionStatusCalculator {

    private UnitPositionStatusCalculator() {
    }

    public static UnitPositionStatus compute(BigDecimal netPosition) {
        int signum = netPosition.signum();
        if (signum > 0) {
            return UnitPositionStatus.OVERDUE;
        }
        if (signum < 0) {
            return UnitPositionStatus.IN_ADVANCE;
        }
        return UnitPositionStatus.UP_TO_DATE;
    }
}
