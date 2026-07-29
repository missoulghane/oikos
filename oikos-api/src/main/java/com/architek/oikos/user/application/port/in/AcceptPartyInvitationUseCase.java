package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.AcceptPartyInvitationCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface AcceptPartyInvitationUseCase {

    UserId accept(AcceptPartyInvitationCommand command);
}
