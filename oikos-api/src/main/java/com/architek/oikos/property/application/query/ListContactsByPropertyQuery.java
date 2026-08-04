package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * search filters contacts by party full name or phone (case-insensitive
 * substring); null means no filter on this axis. Pagination applies to the
 * number of distinct parties (contact groups), not to the flattened
 * unit-ownership rows returned in content.
 */
public record ListContactsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest, String search) {
}
