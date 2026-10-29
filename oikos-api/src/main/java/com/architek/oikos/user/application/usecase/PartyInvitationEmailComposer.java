package com.architek.oikos.user.application.usecase;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Composes the subject/body (template email/party-invitation.html) of the owner-invitation email. Package-private:
 * only used internally by InvitePartyService.
 */
@Component
class PartyInvitationEmailComposer {

    private final String partyInvitationBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    PartyInvitationEmailComposer(@Value("${oikos.mail.party-invitation-base-url}") String partyInvitationBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.partyInvitationBaseUrl = partyInvitationBaseUrl;
        this.templateRenderer = templateRenderer;
    }

    String subject() {
        return "Daba Syndic - Invitation à accéder à votre espace copropriétaire";
    }

    String htmlBody(String fullName, String token) {
        String link = partyInvitationBaseUrl + "?token=" + token;
        return templateRenderer.render("party-invitation", Map.of("fullName", fullName, "link", link));
    }
}
