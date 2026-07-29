package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.application.port.in.InvitePartyUseCase;

@Component
public class PropertyAccountLinkingAdapter implements AccountLinkingPort {

    private final InvitePartyUseCase invitePartyUseCase;

    public PropertyAccountLinkingAdapter(InvitePartyUseCase invitePartyUseCase) {
        this.invitePartyUseCase = invitePartyUseCase;
    }

    @Override
    public void inviteOwnerIfUnlinked(EntityId partyId, EmailVO email, String fullName) {
        invitePartyUseCase.invite(new InvitePartyCommand(partyId, email, fullName));
    }
}
