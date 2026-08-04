package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.ListInvitationsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListInvitationsUseCase {

    Page<InvitationView> listInvitations(ListInvitationsQuery query);
}
