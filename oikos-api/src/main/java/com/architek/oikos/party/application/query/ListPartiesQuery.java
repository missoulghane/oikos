package com.architek.oikos.party.application.query;

import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListPartiesQuery(PageRequest pageRequest, PartySearchCriteria criteria) {
}
