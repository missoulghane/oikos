package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface RegisterUserUseCase {

    UserId register(RegisterUserCommand command);
}
