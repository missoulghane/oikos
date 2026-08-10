package com.architek.oikos.installment.domain.valueobject;

/**
 * Spec &sect;4.3: the unit's net position (creance - avance) is a management
 * view, distinct from - and never compensating - the two separate ledger
 * balances (ROLE_UNIT_RECEIVABLE debit / ROLE_UNIT_ADVANCE credit) that feed
 * the bilan.
 */
public enum UnitPositionStatus {
    OVERDUE,
    UP_TO_DATE,
    IN_ADVANCE
}
