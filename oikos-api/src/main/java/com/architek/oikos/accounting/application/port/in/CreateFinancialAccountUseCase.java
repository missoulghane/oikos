package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.CreateFinancialAccountCommand;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;

public interface CreateFinancialAccountUseCase {

    FinancialAccountId create(CreateFinancialAccountCommand command);
}
