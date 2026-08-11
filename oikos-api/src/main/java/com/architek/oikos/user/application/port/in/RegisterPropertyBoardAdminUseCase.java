package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterPropertyBoardAdminCommand;
import com.architek.oikos.user.application.dto.RegisteredBoardAdminView;

public interface RegisterPropertyBoardAdminUseCase {

    RegisteredBoardAdminView register(RegisterPropertyBoardAdminCommand command);
}
