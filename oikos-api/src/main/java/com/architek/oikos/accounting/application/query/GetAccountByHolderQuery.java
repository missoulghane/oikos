package com.architek.oikos.accounting.application.query;

import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GetAccountByHolderQuery(EntityId holderId, AccountType accountType) {
}
