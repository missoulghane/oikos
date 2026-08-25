package com.architek.oikos.invitation.application.event;

import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Publié quand un membre du syndic a tranché une demande d'adhésion, y compris
 * pour les demandes concurrentes automatiquement rejetées quand un candidat est
 * retenu sur le lot (voir AcceptMembershipRequestService).
 *
 * <p>L'événement porte tout ce dont l'annonce a besoin plutôt qu'un simple id :
 * son auditeur travaille après le commit, où relire la demande passerait par
 * une transaction déjà en cours d'achèvement (voir
 * MembershipDecisionNotificationListener).
 *
 * @param reason motif du refus, nul sur une validation comme sur un refus sans motif
 */
public record MembershipRequestDecidedEvent(MembershipRequestId requestId, EntityId propertyId, EntityId unitId,
                                             EntityId requesterUserId, boolean accepted, EntityId decidedByUserId,
                                             String reason) {
}
