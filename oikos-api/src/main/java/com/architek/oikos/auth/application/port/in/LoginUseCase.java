package com.architek.oikos.auth.application.port.in;

import com.architek.oikos.auth.application.command.LoginCommand;
import com.architek.oikos.auth.application.dto.AuthTokens;

public interface LoginUseCase {

    AuthTokens login(LoginCommand command);
}
