package com.architek.oikos.auth.application.port.in;

import com.architek.oikos.auth.application.command.RequestPasswordResetCommand;

public interface RequestPasswordResetUseCase {

    void requestReset(RequestPasswordResetCommand command);
}
