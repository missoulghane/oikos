package com.architek.oikos.installment.infrastructure.persistence;

import java.math.BigDecimal;

/**
 * The aggregate row of InstallmentJpaRepository#summariseCollectible, mapped to
 * the domain summary by the adapter - a record rather than {@code Object[]},
 * whose shape nothing checks and whose casts are written out at every call.
 */
public record InstallmentCollectionProjection(long count, BigDecimal amount) {
}
