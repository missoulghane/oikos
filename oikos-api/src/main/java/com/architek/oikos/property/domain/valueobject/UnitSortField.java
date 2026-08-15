package com.architek.oikos.property.domain.valueobject;

/**
 * Columns a lot listing can be ordered on. A closed set rather than a free
 * property name: the sort comes from a query parameter, and anything the client
 * can name it can also use to probe the model.
 *
 * Only fields the unit itself carries. Ordering by owner would mean ordering on
 * data that lives behind PartyDirectoryPort, whose page is not the unit page.
 */
public enum UnitSortField {
    UNIT_NUMBER,
    SHARES
}
