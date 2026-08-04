package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.application.port.in.ListInvitationsUseCase;
import com.architek.oikos.invitation.application.query.ListInvitationsQuery;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListInvitationsService implements ListInvitationsUseCase {

    private final InvitationRepository invitationRepository;
    private final InvitationLinkComposer linkComposer;

    public ListInvitationsService(InvitationRepository invitationRepository, InvitationLinkComposer linkComposer) {
        this.invitationRepository = invitationRepository;
        this.linkComposer = linkComposer;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvitationView> listInvitations(ListInvitationsQuery query) {
        return invitationRepository.findAllByPropertyId(query.propertyId(), query.pageRequest())
                .map(invitation -> InvitationView.from(invitation, linkComposer.link(invitation.getToken())));
    }
}
