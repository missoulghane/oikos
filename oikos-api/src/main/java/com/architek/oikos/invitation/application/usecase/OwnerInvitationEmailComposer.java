package com.architek.oikos.invitation.application.usecase;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Compose l'email qui porte une invitation privée à son destinataire.
 * Package-private : n'est utilisé que par CreateInvitationService.
 *
 * <p>Le lot est nommé dans le corps, pas seulement derrière le lien : c'est ce
 * qui permet au destinataire de reconnaître de quoi on lui parle avant de
 * cliquer, et de repérer une erreur du syndic sans avoir à ouvrir la page.
 * Texte et mise en forme : templates/email/owner-invitation.html.
 */
@Component
class OwnerInvitationEmailComposer {

    private final EmailTemplateRendererPort templateRenderer;

    OwnerInvitationEmailComposer(EmailTemplateRendererPort templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    String subject(String propertyName) {
        return "Daba Syndic - Rejoignez " + propertyName;
    }

    String htmlBody(String propertyName, String unitLabel, String link) {
        return templateRenderer.render("owner-invitation",
                Map.of("propertyName", propertyName, "unitLabel", unitLabel, "link", link));
    }
}
