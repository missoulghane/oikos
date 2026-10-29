package com.architek.oikos.invitation.application.usecase;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Compose l'email annonçant au demandeur l'issue de sa demande d'adhésion.
 * Package-private : n'est utilisé que par MembershipDecisionNotifier. Texte et
 * mise en forme : templates/email/membership-accepted.html et membership-rejected.html.
 */
@Component
class MembershipDecisionEmailComposer {

    private final String publicBaseUrl;
    private final EmailTemplateRendererPort templateRenderer;

    MembershipDecisionEmailComposer(@Value("${oikos.mail.public-base-url}") String publicBaseUrl,
            EmailTemplateRendererPort templateRenderer) {
        this.publicBaseUrl = publicBaseUrl;
        this.templateRenderer = templateRenderer;
    }

    String acceptedSubject(String propertyName) {
        return "Daba Syndic - Votre demande d'adhésion à " + propertyName + " est validée";
    }

    String rejectedSubject(String propertyName) {
        return "Daba Syndic - Votre demande d'adhésion à " + propertyName;
    }

    String acceptedHtmlBody(String fullName, String propertyName, String unitNumber) {
        return templateRenderer.render("membership-accepted", Map.of("fullName", fullName, "unitNumber", unitNumber,
                "propertyName", propertyName, "spaceLink", publicBaseUrl + "/dashboard"));
    }

    /**
     * Le motif n'est pas obligatoire côté syndic (voir
     * RejectMembershipRequestCommand) : sans lui, l'email n'invente rien et se
     * contente d'ouvrir la porte d'une reprise de contact.
     */
    String rejectedHtmlBody(String fullName, String propertyName, String unitNumber, String reason) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("unitNumber", unitNumber);
        variables.put("propertyName", propertyName);
        variables.put("reason", reason == null || reason.isBlank() ? null : reason);
        return templateRenderer.render("membership-rejected", variables);
    }
}
