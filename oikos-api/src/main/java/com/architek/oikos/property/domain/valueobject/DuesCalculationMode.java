package com.architek.oikos.property.domain.valueobject;

/**
 * Mode de calcul des cotisations d'une property. FLAT_RATE facture un
 * montant fixe par type de lot (voir UnitTypePricing). SHARES repartit le
 * ProjectedBudget de la property au prorata des tantiemes (Unit.shares) de
 * chaque lot.
 */
public enum DuesCalculationMode {
    FLAT_RATE,
    SHARES
}
