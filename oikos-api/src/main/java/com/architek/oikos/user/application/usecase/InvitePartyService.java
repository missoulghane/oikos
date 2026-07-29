package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.application.port.in.InvitePartyUseCase;
import com.architek.oikos.user.domain.model.PartyInvitationToken;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.service.PartyInvitationTokenGenerator;

/**
 * Idempotent: a party already linked to an AppUser is left untouched (no
 * duplicate invitation). Re-inviting an unlinked party supersedes any
 * still-outstanding token, mirroring RequestPasswordResetService.
 */
@Component
public class InvitePartyService implements InvitePartyUseCase {

    private final UserRepository userRepository;
    private final PartyInvitationTokenRepository partyInvitationTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final PartyInvitationTokenGenerator tokenGenerator;
    private final PartyInvitationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration invitationTokenTtl;

    public InvitePartyService(UserRepository userRepository,
                               PartyInvitationTokenRepository partyInvitationTokenRepository,
                               EmailSenderPort emailSenderPort,
                               PartyInvitationTokenGenerator tokenGenerator,
                               PartyInvitationEmailComposer emailComposer,
                               Clock clock,
                               @Value("${oikos.mail.party-invitation-token-ttl-hours}") long invitationTokenTtlHours) {
        this.userRepository = userRepository;
        this.partyInvitationTokenRepository = partyInvitationTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.invitationTokenTtl = Duration.ofHours(invitationTokenTtlHours);
    }

    @Override
    @Transactional
    public boolean invite(InvitePartyCommand command) {
        if (userRepository.existsByLinkedPartyId(command.partyId())) {
            return false;
        }
        partyInvitationTokenRepository.deleteByPartyId(command.partyId());
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(invitationTokenTtl);
        PartyInvitationToken invitationToken = PartyInvitationToken.issue(
                command.partyId(), command.email(), command.fullName(), rawToken, expiresAt);
        partyInvitationTokenRepository.save(invitationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(command.fullName(), rawToken));
        return true;
    }
}
