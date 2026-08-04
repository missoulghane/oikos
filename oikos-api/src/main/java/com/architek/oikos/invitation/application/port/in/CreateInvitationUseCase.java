package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;

public interface CreateInvitationUseCase {

    InvitationId create(CreateInvitationCommand command);
}
