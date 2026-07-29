package com.architek.oikos.user.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Composes the subject/body of the owner-invitation email. Package-private:
 * only used internally by InvitePartyService.
 */
@Component
class PartyInvitationEmailComposer {

    private final String partyInvitationBaseUrl;

    PartyInvitationEmailComposer(@Value("${oikos.mail.party-invitation-base-url}") String partyInvitationBaseUrl) {
        this.partyInvitationBaseUrl = partyInvitationBaseUrl;
    }

    String subject() {
        return "OIKOS - Invitation à accéder à votre espace copropriétaire";
    }

    String htmlBody(String fullName, String token) {
        String link = partyInvitationBaseUrl + "?token=" + token;
        return """
                <p>Bonjour %s,</p>
                <p>Vous avez été rattaché(e) comme copropriétaire dans OIKOS.</p>
                <p>Cliquez sur le lien ci-dessous pour accéder à votre espace :</p>
                <p><a href="%s">Accéder à mon espace</a></p>
                <p>Ce lien expire dans 7 jours. Si vous n'attendiez pas cet email, ignorez-le.</p>
                """.formatted(fullName, link);
    }
}
