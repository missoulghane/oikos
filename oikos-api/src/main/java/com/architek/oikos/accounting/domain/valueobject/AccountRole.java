package com.architek.oikos.accounting.domain.valueobject;

/**
 * Functional role a LedgerAccount plays for a property (spec &sect;3.2).
 * Business rules (P1-P10) always reference a role, never a hardcoded
 * account number - resolved to a concrete LedgerAccount through
 * LedgerAccount.role()/propertyId()/unitId(), never a table of its own
 * (ADR 0001, "Affinements de schema").
 */
public enum AccountRole {
    UNIT_RECEIVABLE,
    UNIT_ADVANCE,
    DUES_INCOME,
    BANK,
    CASH,
    SUPPLIER,
    STAFF_PAYABLE
}
