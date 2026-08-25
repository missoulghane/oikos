package com.architek.oikos.invitation.application.command;

import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * {@code targetPartyId} et {@code unitId} vont de pair : une invitation
 * PRIVATE part de la fiche d'un contact, pour l'un de ses lots. Tous deux
 * obligatoires pour PRIVATE, interdits pour un lien PUBLIC qui circule, ne
 * s'adresse à personne et laisse chacun choisir son lot.
 *
 * <p>Pas d'adresse email ici : elle est lue sur le contact plutôt que fournie
 * par l'appelant. Une adresse venue du client pourrait ne pas être la sienne,
 * et le lien partirait à côté.
 */
public record CreateInvitationCommand(EntityId propertyId, InvitationType type, EntityId targetPartyId, EntityId unitId,
                                       EntityId createdByUserId) {
}
