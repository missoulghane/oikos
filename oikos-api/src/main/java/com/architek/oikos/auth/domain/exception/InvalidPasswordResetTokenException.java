package com.architek.oikos.auth.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

public class InvalidPasswordResetTokenException extends BusinessException implements CodedException {

    public InvalidPasswordResetTokenException(String message) {
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
