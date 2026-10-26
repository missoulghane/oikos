package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterPropertyBoardAdminCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface RegisterPropertyBoardAdminUseCase {

    UserId register(RegisterPropertyBoardAdminCommand command);
}
