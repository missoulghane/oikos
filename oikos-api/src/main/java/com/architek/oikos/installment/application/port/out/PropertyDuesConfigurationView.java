package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.valueobject.DuesCalculationMode;

/**
 * A property's dues calculation mode and, when relevant (SHARES),
 * its projected budget - null in FLAT_RATE mode or when not yet configured.
 */
public record PropertyDuesConfigurationView(DuesCalculationMode mode, BigDecimal projectedBudget) {
}
