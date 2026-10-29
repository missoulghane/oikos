package com.architek.oikos.invitation.application.usecase;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Compose l'email qui porte une invitation au conseil syndical
 * (templates/email/board-invitation.html). Package-private : n'est utilisé que
 * par CreateBoardInvitationService.
 *
 * <p>Le siège est nommé dans le corps, avec le libellé du catalogue : c'est ce
 * qui distingue cette invitation de celle d'un copropriétaire.
 */
@Component
class BoardInvitationEmailComposer {

    private final EmailTemplateRendererPort templateRenderer;

    BoardInvitationEmailComposer(EmailTemplateRendererPort templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    String subject(String propertyName) {
        return "Daba Syndic - Invitation au conseil syndical de " + propertyName;
    }

    String htmlBody(String propertyName, String boardRoleLabel, String link, long ttlDays) {
        return templateRenderer.render("board-invitation", Map.of("propertyName", propertyName,
                "boardRoleLabel", boardRoleLabel, "link", link, "ttlDays", ttlDays));
    }
}
