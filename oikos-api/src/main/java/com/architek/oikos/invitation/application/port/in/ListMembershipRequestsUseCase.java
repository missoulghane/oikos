package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMembershipRequestsUseCase {

    Page<MembershipRequestOverviewView> listMembershipRequests(ListMembershipRequestsQuery query);
}
