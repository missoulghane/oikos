package com.architek.oikos.invitation.application.port.in;

import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.application.query.GetInvitationByTokenQuery;

public interface GetInvitationByTokenUseCase {

    InvitationPreviewView getPreview(GetInvitationByTokenQuery query);
}
