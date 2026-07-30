package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;

public interface ValidateUnitLettrageUseCase {

    void validate(ValidateUnitLettrageCommand command);
}
