package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterPropertyManagerAdminCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface RegisterPropertyManagerAdminUseCase {

    UserId register(RegisterPropertyManagerAdminCommand command);
}
