package com.architek.oikos.invitation.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Compose l'email annonçant au demandeur l'issue de sa demande d'adhésion.
 * Package-private : n'est utilisé que par MembershipDecisionNotifier.
 */
@Component
class MembershipDecisionEmailComposer {

    private final String publicBaseUrl;

    MembershipDecisionEmailComposer(@Value("${oikos.mail.public-base-url}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    String acceptedSubject(String propertyName) {
        return "Daba Syndic - Votre demande d'adhésion à " + propertyName + " est validée";
    }

    String rejectedSubject(String propertyName) {
        return "Daba Syndic - Votre demande d'adhésion à " + propertyName;
    }

    String acceptedHtmlBody(String fullName, String propertyName, String unitNumber) {
        return """
                <p>Bonjour %s,</p>
                <p>Votre demande d'adhésion pour le lot %s de %s vient d'être validée par le syndic.</p>
                <p>La gestion de votre lot est entièrement à vous : appels de fonds, documents,
                assemblées générales et messagerie de la copropriété.</p>
                <p><a href="%s/dashboard">Accéder à mon espace</a></p>
                """.formatted(fullName, unitNumber, propertyName, publicBaseUrl);
    }

    /**
     * Le motif n'est pas obligatoire côté syndic (voir
     * RejectMembershipRequestCommand) : sans lui, l'email n'invente rien et se
     * contente d'ouvrir la porte d'une reprise de contact.
     */
    String rejectedHtmlBody(String fullName, String propertyName, String unitNumber, String reason) {
        String reasonBlock = reason == null || reason.isBlank() ? ""
                : "<p>Motif indiqué par le syndic : %s</p>".formatted(reason);
        return """
                <p>Bonjour %s,</p>
                <p>Votre demande d'adhésion pour le lot %s de %s n'a pas été retenue par le syndic.</p>
                %s
                <p>Si vous pensez qu'il s'agit d'une erreur, rapprochez-vous du syndic de la copropriété.</p>
                """.formatted(fullName, unitNumber, propertyName, reasonBlock);
    }
}
