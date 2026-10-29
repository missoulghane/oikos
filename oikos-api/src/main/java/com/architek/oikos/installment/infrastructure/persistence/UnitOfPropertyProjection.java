package com.architek.oikos.installment.infrastructure.persistence;

import java.util.UUID;

/** A (property, unit) pair read by InstallmentJpaRepository#findUnitsWithOutstandingDue. */
public record UnitOfPropertyProjection(UUID propertyId, UUID unitId) {
}
