package com.architek.oikos.property.infrastructure.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.port.in.CreateInvitationUseCase;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindLinkedPartyIdsUseCase;

/**
 * Rattacher un copropriétaire à un lot et l'inviter, c'est exactement inviter
 * ce contact pour ce lot depuis sa fiche : une invitation PRIVATE du module
 * invitation (voir CreateInvitationService), donc le lien /invitations, l'écran
 * qui annonce la copropriété et le lot, et une demande d'adhésion que le syndic
 * valide.
 *
 * <p>L'ancienne invitation de compte du module user (lien /accept-invitation,
 * qui créait le compte et le rattachait à la fiche sans que personne ne valide
 * rien) n'est plus émise d'ici : c'était le dernier point d'entrée où les deux
 * parcours divergeaient encore.
 */
@Component
public class PropertyAccountLinkingAdapter implements AccountLinkingPort {

    private final CreateInvitationUseCase createInvitationUseCase;
    private final FindLinkedPartyIdsUseCase findLinkedPartyIdsUseCase;

    public PropertyAccountLinkingAdapter(CreateInvitationUseCase createInvitationUseCase,
                                          FindLinkedPartyIdsUseCase findLinkedPartyIdsUseCase) {
        this.createInvitationUseCase = createInvitationUseCase;
        this.findLinkedPartyIdsUseCase = findLinkedPartyIdsUseCase;
    }

    @Override
    public void inviteOwnerForUnitIfUnlinked(EntityId propertyId, EntityId partyId, EntityId unitId,
                                              EntityId invitedByUserId) {
        // Un contact déjà rattaché à un compte n'a rien à accepter : c'était le
        // premier geste de l'ancienne InvitePartyService, il reste vrai ici.
        if (!findLinkedPartyIdsUseCase.findLinkedPartyIds(List.of(partyId)).isEmpty()) {
            return;
        }
        createInvitationUseCase.create(new CreateInvitationCommand(propertyId, InvitationType.PRIVATE, partyId, unitId,
                invitedByUserId));
    }

    @Override
    public Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds) {
        return findLinkedPartyIdsUseCase.findLinkedPartyIds(partyIds);
    }
}
