package com.architek.oikos.property.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Prix courant associe a un UnitTypeDefinition pour une property donnee (ex:
 * 300 pour un appartement, 100 pour un box). Pas de borne haute, pas de
 * negatif: un prix a zero reste un prix valide (type non facture).
 */
public record Price(BigDecimal value) {

    public Price {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }

    public static Price of(BigDecimal value) {
        return new Price(value);
    }
}
