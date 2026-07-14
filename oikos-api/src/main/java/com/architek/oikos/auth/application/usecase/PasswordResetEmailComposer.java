package com.architek.oikos.auth.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Composes the subject/body of the password-reset email. Package-private: only used
 * internally by RequestPasswordResetService.
 */
@Component
class PasswordResetEmailComposer {

    private final String passwordResetBaseUrl;

    PasswordResetEmailComposer(@Value("${oikos.mail.password-reset-base-url}") String passwordResetBaseUrl) {
        this.passwordResetBaseUrl = passwordResetBaseUrl;
    }

    String subject() {
        return "OIKOS - Réinitialisation de votre mot de passe";
    }

    String htmlBody(String token) {
        String link = passwordResetBaseUrl + "?token=" + token;
        return """
                <p>Vous avez demandé la réinitialisation de votre mot de passe OIKOS.</p>
                <p>Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe :</p>
                <p><a href="%s">Réinitialiser mon mot de passe</a></p>
                <p>Ce lien expire dans 1 heure. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                """.formatted(link);
    }
}
