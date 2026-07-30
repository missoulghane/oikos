package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.TransferBetweenFinancialAccountsCommand;

public interface TransferBetweenFinancialAccountsUseCase {

    void transfer(TransferBetweenFinancialAccountsCommand command);
}
