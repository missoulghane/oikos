package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Thrown when a unit's accounting ledger account cannot be resolved via
 * UnitAccountLedgerPort while raising an installment - should never happen
 * once property provisions a unit account for every Unit it creates (spec
 * &sect;7); signals a genuine invariant violation rather than a normal
 * business rejection. Distinct from accounting.domain.exception's own
 * UnitAccountNotFoundException, which installment does not depend on
 * directly (rule 4/6).
 */
public class UnitAccountNotFoundException extends ResourceNotFoundException {

    public UnitAccountNotFoundException(EntityId unitId) {
        super("No accounting unit account found for unit: " + unitId);
    }
}
