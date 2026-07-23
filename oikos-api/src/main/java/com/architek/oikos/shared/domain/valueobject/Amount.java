package com.architek.oikos.shared.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A strictly positive monetary amount, shared across accounting and installment
 * (Movement, Installment, Allocation). Kept distinct from a raw balance (which
 * can be negative or zero): a movement, an installment or an allocation of
 * zero has no meaning.
 */
public record Amount(BigDecimal value) {

    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    public static Amount of(BigDecimal value) {
        return new Amount(value);
    }
}
