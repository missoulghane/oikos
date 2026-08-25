package com.architek.oikos.invitation.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.port.in.GetPublicInvitationUseCase;
import com.architek.oikos.invitation.application.query.GetPublicInvitationQuery;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class GetPublicInvitationService implements GetPublicInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final InvitationLinkComposer linkComposer;

    public GetPublicInvitationService(InvitationRepository invitationRepository, InvitationLinkComposer linkComposer) {
        this.invitationRepository = invitationRepository;
        this.linkComposer = linkComposer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvitationView> getPublicInvitation(GetPublicInvitationQuery query) {
        return invitationRepository.findPublicByPropertyId(query.propertyId())
                .map(invitation -> InvitationView.from(invitation, linkComposer.link(invitation.getToken())));
    }
}
