package com.architek.oikos.accounting.domain.valueobject;

/**
 * Nature of an account's holder. UNIT accounts track what a lot owes/has
 * paid; PROPERTY accounts mirror that same activity from the property's own
 * bookkeeping perspective (opposite direction - see AccountBalanceService).
 */
public enum AccountType {
    UNIT,
    PROPERTY
}
