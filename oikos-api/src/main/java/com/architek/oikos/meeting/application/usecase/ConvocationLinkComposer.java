package com.architek.oikos.meeting.application.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds the confirmation link from a convocation's token. Package-private:
 * only this package's composers use it, same patron as InvitationLinkComposer.
 *
 * <p>The token is URL-encoded rather than interpolated raw. It is Base64-url,
 * so in practice it never needs escaping - but the encoding is what keeps that
 * true if the generator ever changes, and a broken link is not something a
 * copropriétaire reports.
 */
@Component
class ConvocationLinkComposer {

    private final String confirmationBaseUrl;

    ConvocationLinkComposer(@Value("${oikos.mail.convocation-confirmation-base-url}") String confirmationBaseUrl) {
        this.confirmationBaseUrl = confirmationBaseUrl;
    }

    String link(String token) {
        return confirmationBaseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
