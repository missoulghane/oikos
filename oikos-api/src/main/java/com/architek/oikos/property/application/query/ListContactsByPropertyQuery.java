package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * search filters contacts by party full name or phone (case-insensitive
 * substring); hasLinkedAccount keeps only the contacts that already have (TRUE)
 * or do not yet have (FALSE) an active user account linked to their party.
 * null means no filter on this axis, and the two axes combine (AND) when both
 * are set. Pagination applies to the number of distinct parties (contact
 * groups), not to the flattened unit-ownership rows returned in content.
 */
public record ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search,
                                          Boolean hasLinkedAccount) {

    /** Unfiltered-on-account listing, for the callers that only search (or not at all). */
    public ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search) {
        this(propertyId, pageRequest, search, null);
    }
}
