package com.architek.oikos.property.domain.valueobject;

/**
 * Columns a lot listing can be ordered on. A closed set rather than a free
 * property name: the sort comes from a query parameter, and anything the client
 * can name it can also use to probe the model.
 *
 * One per column of the lots table. Type, owners and ownership come from
 * outside the unit row (type definitions, PartyDirectoryPort, UnitOwnership):
 * a sort on them runs in memory over the whole building, as filtering already
 * did, so it orders the building and not only the page shown.
 */
public enum UnitSortField {
    UNIT_NUMBER,
    UNIT_TYPE,
    FLOOR,
    SHARES,
    OWNERS,
    OWNERSHIP_STATUS
}
