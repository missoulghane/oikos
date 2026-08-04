package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.DisableInvitationCommand;

public interface DisableInvitationUseCase {

    void disable(DisableInvitationCommand command);
}
