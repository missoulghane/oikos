package com.architek.oikos.user.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.in.FindOutstandingPartyInvitationUseCase;
import com.architek.oikos.invitation.application.query.FindOutstandingPartyInvitationQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.OutstandingPartyInvitationPort;

@Component
public class OutstandingPartyInvitationAdapter implements OutstandingPartyInvitationPort {

    private final FindOutstandingPartyInvitationUseCase findOutstandingPartyInvitationUseCase;

    public OutstandingPartyInvitationAdapter(FindOutstandingPartyInvitationUseCase findOutstandingPartyInvitationUseCase) {
        this.findOutstandingPartyInvitationUseCase = findOutstandingPartyInvitationUseCase;
    }

    @Override
    public boolean existsFor(EntityId partyId) {
        return findOutstandingPartyInvitationUseCase
                .findOutstanding(new FindOutstandingPartyInvitationQuery(partyId))
                .isPresent();
    }
}
