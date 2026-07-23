package com.architek.oikos.contact.application.query;

import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListContactsQuery(PageRequest pageRequest, ContactSearchCriteria criteria) {
}
