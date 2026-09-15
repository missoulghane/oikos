package com.architek.oikos.auth.domain.exception;

import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Le mot de passe est bon, mais le compte n'a jamais été activé (email de
 * vérification jamais ouvert) ou a été désactivé depuis.
 *
 * <p>Cette exception n'est levée qu'APRÈS la vérification du mot de passe (voir
 * {@link com.architek.oikos.auth.infrastructure.security.PostPasswordUserDetailsChecker}) :
 * seul quelqu'un qui connaît déjà le mot de passe apprend l'état du compte, et il
 * n'apprend donc rien qu'il ne sache déjà. Le dire est ce qui évite à un
 * copropriétaire de croire qu'il s'est trompé de mot de passe alors qu'il lui
 * suffit d'ouvrir son email d'activation.
 */
public class AccountNotActivatedException extends UnauthorizedException implements CodedException {

    public AccountNotActivatedException() {
        super("Account is not activated or has been disabled");
    }

    @Override
    public String errorCode() {
        return ErrorCodes.ACCOUNT_NOT_ACTIVATED;
    }
}
