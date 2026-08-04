package com.architek.oikos.property.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Budget previsionnel total d'une property, utilise comme base de
 * repartition des cotisations quand duesCalculationMode = SHARES.
 * Strictement positif: un budget a zero n'a pas de sens.
 */
public record ProjectedBudget(BigDecimal value) {

    public ProjectedBudget {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("projectedBudget must be positive");
        }
    }

    public static ProjectedBudget of(BigDecimal value) {
        return new ProjectedBudget(value);
    }
}
