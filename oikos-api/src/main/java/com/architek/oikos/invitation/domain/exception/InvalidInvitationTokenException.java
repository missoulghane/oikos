package com.architek.oikos.invitation.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

/**
 * Covers every reason a token can't be used right now (unknown, expired,
 * disabled, already consumed) under one exception with a varying message -
 * deliberately not distinguished by status code, so a public endpoint never
 * gives an anonymous caller an oracle telling "wrong token" apart from
 * "right token, wrong state" (same shape as InvalidPartyInvitationTokenException).
 */
public class InvalidInvitationTokenException extends BusinessException implements CodedException {

    public InvalidInvitationTokenException(String message) {
        super(message);
    }

    /**
     * Le message dit lequel des cas c'est (inconnu, expiré, déjà consommé) - pour
     * les logs. Le code, lui, ne dit que « ce lien ne marche plus » : c'est tout ce
     * que l'interface a besoin de savoir pour proposer d'en redemander un.
     */
    @Override
    public String errorCode() {
        return ErrorCodes.INVALID_LINK;
    }
}
