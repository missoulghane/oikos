package com.architek.oikos.user.application.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Composes the subject/body of the account-verification email. Package-private:
 * only used internally by RegisterUserService and ResendVerificationService.
 */
@Component
class VerificationEmailComposer {

    private final String verificationBaseUrl;

    VerificationEmailComposer(@Value("${oikos.mail.verification-base-url}") String verificationBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
    }

    String subject() {
        return "Daba Syndic - Confirmez votre adresse email";
    }

    /**
     * returnTo (already sanitized by the caller - see RegisterUserService) rides
     * along as a second query param so the frontend can carry the caller back to
     * whatever multi-step flow (e.g. an invitation wizard) sent them to register,
     * once they've verified and logged in. Null when there's nothing to return to.
     */
    String htmlBody(String token, String returnTo) {
        String link = verificationBaseUrl + "?token=" + token
                + (returnTo != null ? "&returnTo=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8) : "");
        return """
                <p>Bienvenue sur Daba Syndic,</p>
                <p>Merci de confirmer votre adresse email en cliquant sur le lien ci-dessous :</p>
                <p><a href="%s">Confirmer mon compte</a></p>
                <p>Ce lien expire dans 24 heures.</p>
                """.formatted(link);
    }
}
