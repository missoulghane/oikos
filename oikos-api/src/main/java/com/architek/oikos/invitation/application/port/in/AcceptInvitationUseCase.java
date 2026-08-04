package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface AcceptInvitationUseCase {

    EntityId accept(AcceptInvitationCommand command);
}
