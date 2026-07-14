package com.architek.oikos.auth.application.port.in;

import com.architek.oikos.auth.application.command.LogoutCommand;

public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
