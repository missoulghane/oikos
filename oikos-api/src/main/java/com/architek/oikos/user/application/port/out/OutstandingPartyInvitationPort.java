package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Dit si une invitation adressée à ce contact court encore. Le jeton
 * d'invitation de compte de ce module n'est plus la seule façon d'inviter
 * quelqu'un : depuis que l'invitation part de la fiche du contact et porte sur
 * un lot, c'est le module invitation qui la détient (voir
 * FindOutstandingPartyInvitationUseCase). Sans ce port, la fiche contact
 * cessait d'afficher « une invitation est en cours » alors qu'il y en avait
 * bien une.
 *
 * <p>Implémenté en déléguant au port-in public d'invitation, jamais à son
 * dépôt (règle 6) - même sens de dépendance que MembershipRequestSubmissionPort.
 */
public interface OutstandingPartyInvitationPort {

    boolean existsFor(EntityId partyId);
}
