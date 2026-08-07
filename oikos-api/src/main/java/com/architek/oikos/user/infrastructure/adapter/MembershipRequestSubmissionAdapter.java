package com.architek.oikos.user.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.MembershipRequestSubmissionPort;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class MembershipRequestSubmissionAdapter implements MembershipRequestSubmissionPort {

    private final SubmitMembershipRequestUseCase submitMembershipRequestUseCase;

    public MembershipRequestSubmissionAdapter(SubmitMembershipRequestUseCase submitMembershipRequestUseCase) {
        this.submitMembershipRequestUseCase = submitMembershipRequestUseCase;
    }

    @Override
    public void submit(String invitationToken, UserId userId, EntityId unitId) {
        submitMembershipRequestUseCase.submit(new SubmitMembershipRequestCommand(invitationToken, userId.value(), unitId));
    }
}
