package com.architek.oikos.accounting.domain.repository;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Allocates the incremental suffix used when provisioning a property/unit
 * ledger account (accountNumber = prefix + zero-padded increment), under a
 * row lock - one independent counter per (property, numberPrefix), starting
 * at 1.
 */
public interface LedgerAccountNumberSequenceRepository {

    int allocateNextIncrement(EntityId propertyId, String numberPrefix);
}
