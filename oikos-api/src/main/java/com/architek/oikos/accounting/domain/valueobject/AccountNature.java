package com.architek.oikos.accounting.domain.valueobject;

/**
 * Determines which financial statement an account feeds (CPC vs bilan) and
 * its conventional normal side (spec &sect;3.1). normalSide is entirely
 * derived from nature - there is deliberately no separate stored field a
 * seed/migration could set inconsistently.
 */
public enum AccountNature {
    BALANCE_ASSET,
    BALANCE_LIABILITY,
    EXPENSE,
    INCOME;

    public EntryDirection normalSide() {
        return switch (this) {
            case BALANCE_ASSET, EXPENSE -> EntryDirection.DEBIT;
            case BALANCE_LIABILITY, INCOME -> EntryDirection.CREDIT;
        };
    }
}
