package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.domain.model.Role;

/**
 * returnTo is an optional, client-supplied relative path (e.g. an invitation
 * wizard's own URL) to carry through the verification email and back to the
 * login page once the account is verified - sanitized in RegisterUserService
 * before it ever reaches an email or a redirect, never trusted as-is.
 *
 * <p>invitationToken/unitId are optional too: when set (only ever for a PUBLIC
 * invitation's "create an account" step), RegisterUserService submits the
 * membership request as part of this same registration, atomically - see its
 * Javadoc for why.
 */
public record RegisterUserCommand(String fullName, EmailVO email, String phone, RawPassword password, Role role,
                                   String returnTo, String invitationToken, EntityId unitId) {
}
