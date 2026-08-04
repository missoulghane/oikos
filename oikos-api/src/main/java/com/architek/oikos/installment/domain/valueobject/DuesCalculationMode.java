package com.architek.oikos.installment.domain.valueobject;

/**
 * Installment's own view of a property's dues calculation mode. Distinct
 * from property.domain.valueobject.DuesCalculationMode, which installment
 * does not depend on directly (rule 4).
 */
public enum DuesCalculationMode {
    FLAT_RATE,
    SHARES
}
