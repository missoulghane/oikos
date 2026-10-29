package com.architek.oikos.user.application.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Composes the subject/body (template email/account-verification.html) of the account-verification email. Package-private:
 * only used internally by RegisterUserService and ResendVerificationService.
 */
@Component
class VerificationEmailComposer {

    private final String verificationBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    VerificationEmailComposer(@Value("${oikos.mail.verification-base-url}") String verificationBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.verificationBaseUrl = verificationBaseUrl;
        this.templateRenderer = templateRenderer;
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
        return templateRenderer.render("account-verification", Map.of("link", link));
    }
}
