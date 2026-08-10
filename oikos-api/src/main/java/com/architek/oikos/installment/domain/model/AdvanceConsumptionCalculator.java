package com.architek.oikos.installment.domain.model;

import java.math.BigDecimal;

/**
 * Spec &sect;4.2, last paragraph: when a new fund call is issued, the unit's
 * available advance is automatically consumed up to the minimum of the
 * available advance and the amount called. Pure and stateless, like
 * InstallmentStatusCalculator.
 */
public final class AdvanceConsumptionCalculator {

    private AdvanceConsumptionCalculator() {
    }

    public static BigDecimal consume(BigDecimal availableAdvance, BigDecimal installmentAmount) {
        return availableAdvance.min(installmentAmount);
    }
}
