package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.ContactSortField;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;

/**
 * search filters contacts by party full name or phone (case-insensitive
 * substring); hasLinkedAccount keeps only the contacts that already have (TRUE)
 * or do not yet have (FALSE) an active user account linked to their party.
 * null means no filter on this axis, and the two axes combine (AND) when both
 * are set. Pagination applies to the number of distinct parties (contact
 * groups), not to the flattened unit-ownership rows returned in content.
 */
public record ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search,
                                          Boolean hasLinkedAccount, ContactSortField sortField,
                                          SortDirection sortDirection) {

    public ListContactsByPropertyQuery {
        sortField = sortField == null ? ContactSortField.FULL_NAME : sortField;
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
    }

    /** Unfiltered-on-account listing, for the callers that only search (or not at all). */
    public ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search) {
        this(propertyId, pageRequest, search, null, null, null);
    }

    public ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search,
                                        Boolean hasLinkedAccount) {
        this(propertyId, pageRequest, search, hasLinkedAccount, null, null);
    }
}
