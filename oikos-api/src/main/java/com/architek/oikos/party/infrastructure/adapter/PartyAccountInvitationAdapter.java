package com.architek.oikos.party.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.party.application.port.out.AccountInvitationPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.application.port.in.InvitePartyUseCase;

/**
 * Adaptateur inter-feature : délègue au port-in public du feature user
 * (InvitePartyUseCase), jamais à son dépôt (règle 6). Même chemin que
 * PropertyAccountLinkingAdapter, pour l'autre porte d'entrée d'un contact.
 */
@Component
public class PartyAccountInvitationAdapter implements AccountInvitationPort {

    private final InvitePartyUseCase invitePartyUseCase;

    public PartyAccountInvitationAdapter(InvitePartyUseCase invitePartyUseCase) {
        this.invitePartyUseCase = invitePartyUseCase;
    }

    @Override
    public void inviteIfUnlinked(EntityId partyId, EmailVO email, String fullName) {
        invitePartyUseCase.invite(new InvitePartyCommand(partyId, email, fullName));
    }
}
