package com.architek.oikos.auth.application.port.in;

import com.architek.oikos.auth.application.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);
}
