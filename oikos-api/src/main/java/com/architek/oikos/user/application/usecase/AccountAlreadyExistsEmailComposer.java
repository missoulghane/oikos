package com.architek.oikos.user.application.usecase;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Composes the email sent instead of a sign-up confirmation when the address
 * already has an account (template email/account-already-exists.html). It is
 * what lets the sign-up endpoints answer the same way whether the address is
 * known or not: the answer to "does this address have an account?" only ever
 * reaches the mailbox's owner.
 */
@Component
class AccountAlreadyExistsEmailComposer {

    private final String publicBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    AccountAlreadyExistsEmailComposer(@Value("${oikos.mail.public-base-url}") String publicBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.publicBaseUrl = publicBaseUrl;
        this.templateRenderer = templateRenderer;
    }

    String subject() {
        return "Daba Syndic - Vous avez déjà un compte";
    }

    String htmlBody() {
        return templateRenderer.render("account-already-exists", Map.of(
                "loginLink", publicBaseUrl + "/login",
                "passwordResetLink", publicBaseUrl + "/forgot-password"));
    }
}
