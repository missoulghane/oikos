package com.architek.oikos.accounting.domain.valueobject;

/**
 * Debit/credit side of a journal entry line. Single shared enum for the
 * whole PCM engine (ADR 0001) - unlike the pre-PCM model, which duplicated
 * a direction enum per ledger (FinancialEntryDirection,
 * UnitAccountMovementDirection).
 */
public enum EntryDirection {
    DEBIT,
    CREDIT
}
