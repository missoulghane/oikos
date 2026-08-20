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
    STAFF_PAYABLE,
    /**
     * Compte de bilan où la clôture d'exercice déverse le résultat, une fois
     * les classes 6 et 7 soldées. Une copropriété n'en a qu'un, créé au
     * premier exercice clôturé plutôt qu'à la création de la copropriété : tant
     * qu'aucun exercice n'est clos, il n'aurait rien à porter.
     */
    RESULT
}
