package com.architek.oikos.property.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Pourcentage de detention d'un unit par un copropriataire (ex: 50.00 pour un
 * couple a parts egales). Borne entre 0 et 100: contrairement aux shares,
 * une part de propriete est bien un pourcentage du unit lui-meme.
 */
public record OwnershipShare(BigDecimal value) {

    private static final BigDecimal MAX = new BigDecimal("100");

    public OwnershipShare {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() < 0 || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("ownershipShare must be between 0 and 100");
        }
    }

    public static OwnershipShare of(BigDecimal value) {
        return new OwnershipShare(value);
    }
}
