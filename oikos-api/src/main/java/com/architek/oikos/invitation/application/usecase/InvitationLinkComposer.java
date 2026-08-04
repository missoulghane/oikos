package com.architek.oikos.invitation.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds the shareable invitation link from a token. Package-private: only
 * used internally by this package's use cases.
 */
@Component
class InvitationLinkComposer {

    private final String invitationBaseUrl;

    InvitationLinkComposer(@Value("${oikos.mail.invitation-base-url}") String invitationBaseUrl) {
        this.invitationBaseUrl = invitationBaseUrl;
    }

    String link(String token) {
        return invitationBaseUrl + "?token=" + token;
    }
}
