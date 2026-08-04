package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.application.query.GetMembershipRequestQuery;

public interface GetMembershipRequestUseCase {

    MembershipRequestView getMembershipRequest(GetMembershipRequestQuery query);
}
