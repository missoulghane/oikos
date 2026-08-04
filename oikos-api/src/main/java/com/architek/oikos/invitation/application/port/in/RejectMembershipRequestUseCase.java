package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.RejectMembershipRequestCommand;

public interface RejectMembershipRequestUseCase {

    void reject(RejectMembershipRequestCommand command);
}
