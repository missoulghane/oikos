package com.architek.oikos.invitation.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Either actingUserId is set (the request carried a valid session - the
 * caller resolves/creates their own party under that identity, no new
 * account), or email/fullName/password are set (anonymous - a brand-new
 * account is provisioned). Never both, never neither; validated in
 * AcceptInvitationService. unitId is only used (and required) for
 * PRIVATE_WITHOUT_UNIT invitations - ignored for PRIVATE_WITH_UNIT, whose
 * unit is fixed by the invitation itself.
 */
public record AcceptInvitationCommand(String token, EntityId actingUserId, EmailVO email, String fullName,
                                       RawPassword password, EntityId unitId) {
}
