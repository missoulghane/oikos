package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.RegisterPropertyManagerAdminCommand;

public interface RegisterPropertyManagerAdminUseCase {

    /**
     * Answers the same way whether the address is new or already has an
     * account: an existing one only gets an email saying so (see
     * RegisterPropertyBoardAdminService for why).
     */
    void register(RegisterPropertyManagerAdminCommand command);
}
