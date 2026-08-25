package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;

/**
 * Compose l'email qui porte une invitation privée à son destinataire.
 * Package-private : n'est utilisé que par CreateInvitationService.
 *
 * <p>Le lot est nommé dans le corps, pas seulement derrière le lien : c'est ce
 * qui permet au destinataire de reconnaître de quoi on lui parle avant de
 * cliquer, et de repérer une erreur du syndic sans avoir à ouvrir la page.
 */
@Component
class OwnerInvitationEmailComposer {

    String subject(String propertyName) {
        return "Daba Syndic - Rejoignez " + propertyName;
    }

    String htmlBody(String propertyName, String unitLabel, String link) {
        return """
                <p>Bonjour,</p>
                <p>Le syndic de %s vous invite à rejoindre votre espace copropriétaire pour le lot %s.</p>
                <p><a href="%s">Rejoindre ma copropriété</a></p>
                <p>Vous confirmerez votre lot, puis créerez votre compte. Le syndic validera ensuite votre
                demande pour sécuriser l'accès.</p>
                <p>Si ce lot n'est pas le vôtre, vous pourrez en choisir un autre depuis cette même page.</p>
                """.formatted(propertyName, unitLabel, link);
    }
}
