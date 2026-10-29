package com.architek.oikos.auth.application.usecase;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Composes the subject/body (template email/password-reset.html) of the password-reset email. Package-private: only used
 * internally by RequestPasswordResetService.
 */
@Component
class PasswordResetEmailComposer {

    private final String passwordResetBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    PasswordResetEmailComposer(@Value("${oikos.mail.password-reset-base-url}") String passwordResetBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.passwordResetBaseUrl = passwordResetBaseUrl;
        this.templateRenderer = templateRenderer;
    }

    String subject() {
        return "Daba Syndic - Réinitialisation de votre mot de passe";
    }

    String htmlBody(String token) {
        String link = passwordResetBaseUrl + "?token=" + token;
        return templateRenderer.render("password-reset", Map.of("link", link));
    }
}
