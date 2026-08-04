package com.architek.oikos.invitation.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import com.architek.oikos.invitation.domain.model.InvitationType;

/**
 * unitId/targetEmail requirements vary by type (mandatory for
 * PRIVATE_WITH_UNIT/PRIVATE_*, absent for PUBLIC) - validated in
 * CreateInvitationService rather than declaratively here, since bean
 * validation can't express "required depending on another field's value".
 */
public record CreateInvitationRequest(@NotNull InvitationType type, String unitId, @Email String targetEmail) {
}
