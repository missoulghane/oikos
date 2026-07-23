package com.architek.oikos.accounting.application.query;

import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListMovementsQuery(AccountId accountId, PageRequest pageRequest) {
}
