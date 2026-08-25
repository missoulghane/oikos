package com.architek.oikos.user.application.port.in;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyAccountStatus;

/**
 * Public entry point used by party (la fiche contact) to savoir si un contact
 * est deja rattache a un compte, seulement invite, ou ni l'un ni l'autre -
 * sans dependre du repository user directement (regle 6). Pendant unitaire de
 * {@link FindLinkedPartyIdsUseCase}, qui ne repond que "rattache ou non" et
 * ignore les invitations en cours.
 */
public interface GetPartyAccountStatusUseCase {

    PartyAccountStatus statusOf(EntityId partyId);
}
