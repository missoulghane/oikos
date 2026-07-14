package com.architek.oikos.auth.application.command;

import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * identifier can be the account's own login, or the email/phone of its linked
 * contact - see user.application.usecase.LoadUserByIdentifierService for the
 * resolution priority.
 */
public record LoginCommand(String identifier, RawPassword password) {
}
