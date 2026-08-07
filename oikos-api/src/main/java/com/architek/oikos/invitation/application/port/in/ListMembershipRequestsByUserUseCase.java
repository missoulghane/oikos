package com.architek.oikos.invitation.application.port.in;

import java.util.List;

import com.architek.oikos.invitation.application.dto.MembershipRequestSummaryView;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsByUserQuery;

public interface ListMembershipRequestsByUserUseCase {

    List<MembershipRequestSummaryView> listMembershipRequests(ListMembershipRequestsByUserQuery query);
}
