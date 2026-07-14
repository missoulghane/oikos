package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.DeleteUserCommand;

public interface DeleteUserUseCase {

    void delete(DeleteUserCommand command);
}
