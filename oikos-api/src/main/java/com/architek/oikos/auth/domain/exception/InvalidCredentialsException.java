package com.architek.oikos.auth.domain.exception;

import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Identifiant inconnu, ou mot de passe faux : les deux cas se répondent
 * exactement de la même façon, sans quoi la page de connexion devient un moyen
 * de savoir quelles adresses ont un compte ici.
 */
public class InvalidCredentialsException extends UnauthorizedException implements CodedException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    @Override
    public String errorCode() {
        return ErrorCodes.INVALID_CREDENTIALS;
    }
}
