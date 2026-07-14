package com.architek.oikos.auth.application.port.in;

import com.architek.oikos.auth.application.command.RefreshTokenCommand;
import com.architek.oikos.auth.application.dto.AuthTokens;

public interface RefreshTokenUseCase {

    AuthTokens refresh(RefreshTokenCommand command);
}
