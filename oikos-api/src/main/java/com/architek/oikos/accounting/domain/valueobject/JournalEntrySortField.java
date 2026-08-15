package com.architek.oikos.accounting.domain.valueobject;

/**
 * Columns a treasury operations listing can be ordered on. A closed set rather
 * than a free property name, for the same reason as the other sort fields: the
 * value comes straight from a query parameter.
 *
 * The displayed amount is absent on purpose - it is summed from the entry's
 * debit lines, so it is not a column the database can order on.
 */
public enum JournalEntrySortField {
    PIECE_DATE,
    PIECE_NUMBER
}
