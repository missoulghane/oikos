package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.DisableInvitationCommand;
import com.architek.oikos.invitation.application.port.in.DisableInvitationUseCase;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

@Component
public class DisableInvitationService implements DisableInvitationUseCase {

    private final InvitationRepository invitationRepository;

    public DisableInvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    @Transactional
    public void disable(DisableInvitationCommand command) {
        Invitation invitation = invitationRepository.findById(command.id())
                .orElseThrow(() -> new InvitationNotFoundException(command.id()));
        invitationRepository.save(invitation.disable());
    }
}
