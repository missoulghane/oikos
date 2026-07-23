package com.architek.oikos.accounting.application.command;

import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateAccountCommand(EntityId holderId, AccountType accountType) {
}
