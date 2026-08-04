package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;

public interface SubmitMembershipRequestUseCase {

    MembershipRequestId submit(SubmitMembershipRequestCommand command);
}
