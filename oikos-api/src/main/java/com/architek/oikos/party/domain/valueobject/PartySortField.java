package com.architek.oikos.party.domain.valueobject;

/**
 * Columns a party listing can be ordered on. A closed set rather than a free
 * property name: the sort comes from a query parameter, and an arbitrary name
 * would let a client order on - and so probe - any column of the entity.
 */
public enum PartySortField {
    FULL_NAME,
    EMAIL
}
