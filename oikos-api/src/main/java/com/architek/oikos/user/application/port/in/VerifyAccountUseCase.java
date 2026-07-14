package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.VerifyAccountCommand;

public interface VerifyAccountUseCase {

    void verify(VerifyAccountCommand command);
}
