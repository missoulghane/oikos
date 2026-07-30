package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.ValidateBulkLettrageCommand;

public interface ValidateBulkLettrageUseCase {

    void validate(ValidateBulkLettrageCommand command);
}
