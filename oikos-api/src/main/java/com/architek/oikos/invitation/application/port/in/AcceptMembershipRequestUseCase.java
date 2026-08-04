package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.AcceptMembershipRequestCommand;

public interface AcceptMembershipRequestUseCase {

    void accept(AcceptMembershipRequestCommand command);
}
