package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.port.in.FindOutstandingPartyInvitationUseCase;
import com.architek.oikos.invitation.application.query.FindOutstandingPartyInvitationQuery;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class FindOutstandingPartyInvitationService implements FindOutstandingPartyInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final InvitationLinkComposer linkComposer;
    private final Clock clock;

    public FindOutstandingPartyInvitationService(InvitationRepository invitationRepository,
                                                   InvitationLinkComposer linkComposer, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.linkComposer = linkComposer;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvitationView> findOutstanding(FindOutstandingPartyInvitationQuery query) {
        // ACTIVE ne suffit pas : il n'existe pas de statut EXPIRED stocké, et
        // une invitation périmée n'est plus « en cours » pour personne.
        return invitationRepository.findOutstandingPrivateByPartyId(query.partyId())
                .filter(invitation -> invitation.isUsable(clock.instant()))
                .map(invitation -> InvitationView.from(invitation, linkComposer.link(invitation.getToken())));
    }
}
