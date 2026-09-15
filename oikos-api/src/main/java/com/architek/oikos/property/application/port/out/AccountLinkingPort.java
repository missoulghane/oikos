package com.architek.oikos.property.application.port.out;

import java.util.Collection;
import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to invite a unit owner's Party to join the property for
 * the lot it was just attached to, and to check which parties already have an
 * account. Implemented in property.infrastructure.adapter by delegating to
 * invitation's public port-in use cases - never to their repositories
 * directly (rule 6).
 */
public interface AccountLinkingPort {

    /**
     * Émet l'invitation privée du contact pour ce lot - le même lien, le même
     * écran et la même demande d'adhésion que depuis la fiche du contact (voir
     * InviteContactForLotModal, côté web). Sans effet si le contact a déjà un
     * compte : il n'a rien à accepter.
     *
     * <p>Ni nom ni adresse ici : le module invitation les lit sur la fiche du
     * contact plutôt que de les recevoir de l'appelant, une adresse venue
     * d'ailleurs pouvant ne pas être la sienne.
     */
    void inviteOwnerForUnitIfUnlinked(EntityId propertyId, EntityId partyId, EntityId unitId, EntityId invitedByUserId);

    /**
     * @return the subset of {@code partyIds} that already have a linked AppUser account.
     */
    Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds);
}
