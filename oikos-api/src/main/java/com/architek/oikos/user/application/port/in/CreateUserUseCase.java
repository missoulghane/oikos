package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.CreateUserCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface CreateUserUseCase {

    UserId create(CreateUserCommand command);
}
