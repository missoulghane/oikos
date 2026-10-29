package com.architek.oikos.user.application.usecase;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Composes the subject/body (template email/account-activation.html) of the account-activation email sent to users
 * created by an administrator. Package-private: only used internally by
 * CreateUserService and ResendAccountActivationService.
 */
@Component
class AccountActivationEmailComposer {

    private final String accountActivationBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    AccountActivationEmailComposer(@Value("${oikos.mail.account-activation-base-url}") String accountActivationBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.accountActivationBaseUrl = accountActivationBaseUrl;
        this.templateRenderer = templateRenderer;
    }

    String subject() {
        return "Daba Syndic - Activez votre compte";
    }

    String htmlBody(String token) {
        String link = accountActivationBaseUrl + "?token=" + token;
        return templateRenderer.render("account-activation", Map.of("link", link));
    }
}
