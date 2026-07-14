package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ResendVerificationCommand;

public interface ResendVerificationUseCase {

    void resend(ResendVerificationCommand command);
}
