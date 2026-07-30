package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordExceptionalDepositCommand;

public interface RecordExceptionalDepositUseCase {

    void record(RecordExceptionalDepositCommand command);
}
