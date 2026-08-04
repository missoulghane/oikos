package com.architek.oikos.invitation.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Same actingUserId-vs-email/fullName/password branching as
 * AcceptInvitationCommand - see AcceptInvitationService's doc for why.
 */
public record SubmitMembershipRequestCommand(String token, EntityId actingUserId, EmailVO email, String fullName,
                                              RawPassword password, EntityId unitId) {
}
