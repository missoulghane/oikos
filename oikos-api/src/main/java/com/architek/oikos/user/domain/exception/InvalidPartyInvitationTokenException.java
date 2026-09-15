package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

public class InvalidPartyInvitationTokenException extends BusinessException implements CodedException {

    public InvalidPartyInvitationTokenException(String message) {
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
