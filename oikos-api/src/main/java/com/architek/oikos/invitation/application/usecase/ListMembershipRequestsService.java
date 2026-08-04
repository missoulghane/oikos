package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsUseCase;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListMembershipRequestsService implements ListMembershipRequestsUseCase {

    private final MembershipRequestRepository membershipRequestRepository;

    public ListMembershipRequestsService(MembershipRequestRepository membershipRequestRepository) {
        this.membershipRequestRepository = membershipRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipRequestView> listMembershipRequests(ListMembershipRequestsQuery query) {
        return membershipRequestRepository.findAllByPropertyId(query.propertyId(), query.pageRequest())
                .map(MembershipRequestView::from);
    }
}
