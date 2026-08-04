package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.port.in.GetMembershipRequestUseCase;
import com.architek.oikos.invitation.application.query.GetMembershipRequestQuery;
import com.architek.oikos.invitation.domain.exception.MembershipRequestNotFoundException;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;

@Component
public class GetMembershipRequestService implements GetMembershipRequestUseCase {

    private final MembershipRequestRepository membershipRequestRepository;

    public GetMembershipRequestService(MembershipRequestRepository membershipRequestRepository) {
        this.membershipRequestRepository = membershipRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipRequestView getMembershipRequest(GetMembershipRequestQuery query) {
        MembershipRequest request = membershipRequestRepository.findById(query.id())
                .orElseThrow(() -> new MembershipRequestNotFoundException(query.id()));
        return MembershipRequestView.from(request);
    }
}
