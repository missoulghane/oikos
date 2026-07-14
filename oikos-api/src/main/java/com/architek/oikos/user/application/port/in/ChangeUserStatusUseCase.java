package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ChangeUserStatusCommand;
import com.architek.oikos.user.application.dto.UserView;

public interface ChangeUserStatusUseCase {

    UserView changeStatus(ChangeUserStatusCommand command);
}
