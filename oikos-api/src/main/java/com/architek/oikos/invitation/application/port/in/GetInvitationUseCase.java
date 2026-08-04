package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.GetInvitationQuery;

public interface GetInvitationUseCase {

    InvitationView getInvitation(GetInvitationQuery query);
}
