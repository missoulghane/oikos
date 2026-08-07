package com.architek.oikos.user.application.port.in;

import java.util.List;

import com.architek.oikos.user.application.port.out.OwnedMembershipRequestView;
import com.architek.oikos.user.application.query.GetMyMembershipRequestsQuery;

public interface GetMyMembershipRequestsUseCase {

    List<OwnedMembershipRequestView> getMyMembershipRequests(GetMyMembershipRequestsQuery query);
}
