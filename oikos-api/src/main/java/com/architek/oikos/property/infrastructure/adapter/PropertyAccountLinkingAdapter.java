package com.architek.oikos.property.infrastructure.adapter;

import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.application.port.in.FindLinkedPartyIdsUseCase;
import com.architek.oikos.user.application.port.in.InvitePartyUseCase;

@Component
public class PropertyAccountLinkingAdapter implements AccountLinkingPort {

    private final InvitePartyUseCase invitePartyUseCase;
    private final FindLinkedPartyIdsUseCase findLinkedPartyIdsUseCase;

    public PropertyAccountLinkingAdapter(InvitePartyUseCase invitePartyUseCase,
                                          FindLinkedPartyIdsUseCase findLinkedPartyIdsUseCase) {
        this.invitePartyUseCase = invitePartyUseCase;
        this.findLinkedPartyIdsUseCase = findLinkedPartyIdsUseCase;
    }

    @Override
    public void inviteOwnerIfUnlinked(EntityId partyId, EmailVO email, String fullName) {
        invitePartyUseCase.invite(new InvitePartyCommand(partyId, email, fullName));
    }

    @Override
    public Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds) {
        return findLinkedPartyIdsUseCase.findLinkedPartyIds(partyIds);
    }
}
