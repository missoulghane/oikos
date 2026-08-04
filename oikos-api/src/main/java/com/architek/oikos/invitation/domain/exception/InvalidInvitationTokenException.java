package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * Covers every reason a token can't be used right now (unknown, expired,
 * disabled, already consumed) under one exception with a varying message -
 * deliberately not distinguished by status code, so a public endpoint never
 * gives an anonymous caller an oracle telling "wrong token" apart from
 * "right token, wrong state" (same shape as InvalidPartyInvitationTokenException).
 */
public class InvalidInvitationTokenException extends BusinessException {

    public InvalidInvitationTokenException(String message) {
        super(message);
    }
}
