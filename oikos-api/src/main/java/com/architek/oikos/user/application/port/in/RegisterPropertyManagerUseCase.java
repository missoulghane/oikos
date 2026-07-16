package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterPropertyManagerCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface RegisterPropertyManagerUseCase {

    UserId register(RegisterPropertyManagerCommand command);
}
