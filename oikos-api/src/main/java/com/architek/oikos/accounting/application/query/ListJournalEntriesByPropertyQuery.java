package com.architek.oikos.accounting.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListJournalEntriesByPropertyQuery(EntityId propertyId, PageRequest pageRequest) {
}
