package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.domain.valueobject.AccountId;

public interface CreateAccountUseCase {

    AccountId create(CreateAccountCommand command);
}
