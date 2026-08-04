package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListMembershipRequestsUseCase {

    Page<MembershipRequestView> listMembershipRequests(ListMembershipRequestsQuery query);
}
