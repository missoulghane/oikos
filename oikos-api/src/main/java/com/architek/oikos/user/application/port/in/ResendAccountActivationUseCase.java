package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ResendAccountActivationCommand;

public interface ResendAccountActivationUseCase {

    void resend(ResendAccountActivationCommand command);
}
