package com.architek.oikos.user.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Composes the subject/body of the account-activation email sent to users
 * created by an administrator. Package-private: only used internally by
 * CreateUserService and ResendAccountActivationService.
 */
@Component
class AccountActivationEmailComposer {

    private final String accountActivationBaseUrl;

    AccountActivationEmailComposer(@Value("${oikos.mail.account-activation-base-url}") String accountActivationBaseUrl) {
        this.accountActivationBaseUrl = accountActivationBaseUrl;
    }

    String subject() {
        return "Daba Syndic - Activez votre compte";
    }

    String htmlBody(String token) {
        String link = accountActivationBaseUrl + "?token=" + token;
        return """
                <p>Bienvenue sur Daba Syndic,</p>
                <p>Un compte administrateur a été créé pour vous. Cliquez sur le lien ci-dessous pour
                l'activer et choisir votre mot de passe :</p>
                <p><a href="%s">Activer mon compte</a></p>
                <p>Ce lien expire dans 24 heures.</p>
                """.formatted(link);
    }
}
