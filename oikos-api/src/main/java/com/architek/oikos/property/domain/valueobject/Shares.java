package com.architek.oikos.property.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quote-part des charges communes associee a un unit (ex: 150 sur un total de
 * 1000 pour la property). Pas de borne haute: le total de reference est
 * propre a chaque property, pas normalise a 100.
 */
public record Shares(BigDecimal value) {

    public Shares {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("shares must not be negative");
        }
    }

    public static Shares of(BigDecimal value) {
        return new Shares(value);
    }
}
