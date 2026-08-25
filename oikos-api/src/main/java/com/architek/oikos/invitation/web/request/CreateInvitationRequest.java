package com.architek.oikos.invitation.web.request;

import jakarta.validation.constraints.NotNull;
import com.architek.oikos.invitation.domain.model.InvitationType;

/**
 * partyId et unitId sont obligatoires pour PRIVATE et interdits pour PUBLIC -
 * validé dans CreateInvitationService plutôt que déclarativement ici, la
 * validation par annotations ne sachant pas exprimer « obligatoire selon la
 * valeur d'un autre champ ».
 *
 * <p>Pas d'adresse email : elle est lue sur le contact désigné plutôt que
 * fournie par l'appelant, une adresse venue du client pouvant ne pas être la
 * sienne.
 */
public record CreateInvitationRequest(@NotNull InvitationType type, String partyId, String unitId) {
}
