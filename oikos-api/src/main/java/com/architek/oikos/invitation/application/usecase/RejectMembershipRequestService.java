package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.RejectMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.RejectMembershipRequestUseCase;
import com.architek.oikos.invitation.domain.exception.MembershipRequestAlreadyDecidedException;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;

@Component
public class RejectMembershipRequestService implements RejectMembershipRequestUseCase {

    private final MembershipRequestRepository membershipRequestRepository;
    private final Clock clock;

    public RejectMembershipRequestService(MembershipRequestRepository membershipRequestRepository, Clock clock) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void reject(RejectMembershipRequestCommand command) {
        MembershipRequest request = membershipRequestRepository.findById(command.id())
                .orElseThrow(() -> new MembershipRequestNotFoundException(command.id()));
        if (!request.isPending()) {
            throw new MembershipRequestAlreadyDecidedException();
        }
        membershipRequestRepository.save(request.reject(clock.instant(), command.decidedByUserId(), command.reason()));
    }
}
