package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.EnableInvitationCommand;
import com.architek.oikos.invitation.application.port.in.EnableInvitationUseCase;
import com.architek.oikos.invitation.domain.exception.InvitationNotFoundException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;

/**
 * Réactive un lien désactivé sur son propre jeton, échéance repoussée d'un TTL
 * complet (voir Invitation.enable) : un lien rouvert après une longue fermeture
 * reviendrait sinon ACTIVE mais expiré, donc inutilisable, ce que rien dans
 * l'interface ne dirait au syndic.
 */
@Component
public class EnableInvitationService implements EnableInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final Clock clock;
    private final Duration invitationTokenTtl;

    public EnableInvitationService(InvitationRepository invitationRepository, Clock clock,
                                    @Value("${oikos.mail.invitation-token-ttl-days}") long invitationTokenTtlDays) {
        this.invitationRepository = invitationRepository;
        this.clock = clock;
        this.invitationTokenTtl = Duration.ofDays(invitationTokenTtlDays);
    }

    @Override
    @Transactional
    public void enable(EnableInvitationCommand command) {
        Invitation invitation = invitationRepository.findById(command.id())
                .orElseThrow(() -> new InvitationNotFoundException(command.id()));
        invitationRepository.save(invitation.enable(clock.instant().plus(invitationTokenTtl)));
    }
}
