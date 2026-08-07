package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.CreateBoardInvitationCommand;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;

public interface CreateBoardInvitationUseCase {

    InvitationId create(CreateBoardInvitationCommand command);
}
