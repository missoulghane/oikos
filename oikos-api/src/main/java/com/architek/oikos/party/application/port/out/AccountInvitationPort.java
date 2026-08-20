package com.architek.oikos.party.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Envoie au contact qu'on vient d'enregistrer le lien qui rattachera un compte à
 * sa fiche. Implémenté dans party.infrastructure.adapter en déléguant au
 * port-in public du feature user, jamais à son dépôt (règle 6).
 *
 * <p>Sans effet si ce contact a déjà un compte : c'est le use case appelé qui le
 * vérifie, pas l'appelant.
 */
public interface AccountInvitationPort {

    void inviteIfUnlinked(EntityId partyId, EmailVO email, String fullName);
}
