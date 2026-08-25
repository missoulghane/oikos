package com.architek.oikos.invitation.application.port.in;

import java.util.Optional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.query.FindOutstandingPartyInvitationQuery;

/**
 * L'invitation privée qui court encore pour ce contact - celle dont la fiche
 * contact dit « une invitation est en cours ». Optional.empty() dès qu'elle a
 * été acceptée, désactivée, remplacée ou qu'elle a expiré.
 *
 * <p>Exposé comme port-in parce qu'un autre module le lit : user en a besoin
 * pour dire l'état d'un contact (voir PartyAccountStatus.INVITED), et passe
 * par ici plutôt que par le dépôt d'invitation (règle 6).
 */
public interface FindOutstandingPartyInvitationUseCase {

    Optional<InvitationView> findOutstanding(FindOutstandingPartyInvitationQuery query);
}
