package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Dépose la décision du syndic dans « Ma messagerie » du demandeur, en plus de
 * la notification : une notification se lit une fois et disparaît de la
 * cloche, un message reste consultable et, surtout, répondable - un refus sans
 * canal de réponse est un mur.
 *
 * <p>Implémenté en déléguant au port-in public de messaging
 * (StartGroupConversationUseCase), jamais à son dépôt (règle 6).
 */
public interface MessagingPort {

    /**
     * @return {@code true} si le message est bien parti. {@code false} quand
     *         messaging refuse l'envoi - typiquement un demandeur qui n'est pas
     *         (ou pas encore) membre de la copropriété au sens de la messagerie,
     *         ce qui est exactement le cas d'un refus : le rôle n'a jamais été
     *         posé. L'appelant poursuit avec les autres canaux plutôt que de
     *         faire échouer la décision elle-même.
     */
    boolean sendDecisionMessage(EntityId propertyId, EntityId senderUserId, EntityId recipientUserId, String subject,
                                 String body);
}
