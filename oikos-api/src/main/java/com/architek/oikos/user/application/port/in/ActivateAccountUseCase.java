package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ActivateAccountCommand;

public interface ActivateAccountUseCase {

    void activate(ActivateAccountCommand command);
}
