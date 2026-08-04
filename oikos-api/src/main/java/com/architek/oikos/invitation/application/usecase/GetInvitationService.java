package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.port.in.GetInvitationUseCase;
import com.architek.oikos.invitation.application.query.GetInvitationQuery;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class GetInvitationService implements GetInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final InvitationLinkComposer linkComposer;

    public GetInvitationService(InvitationRepository invitationRepository, InvitationLinkComposer linkComposer) {
        this.invitationRepository = invitationRepository;
        this.linkComposer = linkComposer;
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationView getInvitation(GetInvitationQuery query) {
        Invitation invitation = invitationRepository.findById(query.id())
                .orElseThrow(() -> new InvitationNotFoundException(query.id()));
        return InvitationView.from(invitation, linkComposer.link(invitation.getToken()));
    }
}
