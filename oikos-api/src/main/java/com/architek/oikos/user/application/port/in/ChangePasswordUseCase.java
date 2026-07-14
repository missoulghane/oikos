package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ChangePasswordCommand;

public interface ChangePasswordUseCase {

    void changePassword(ChangePasswordCommand command);
}
