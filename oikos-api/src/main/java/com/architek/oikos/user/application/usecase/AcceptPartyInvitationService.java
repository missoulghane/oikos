package com.architek.oikos.user.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.AcceptPartyInvitationCommand;
import com.architek.oikos.user.application.port.in.AcceptPartyInvitationUseCase;
import com.architek.oikos.user.domain.exception.InvalidPartyInvitationTokenException;
import com.architek.oikos.user.domain.model.PartyInvitationToken;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Links the invited Party to an AppUser: an already-existing account
 * (matched by the invitation's own email, e.g. an owner of lots in several
 * properties) is simply linked; otherwise a new account is created and
 * linked in the same step. The token is single-use either way.
 */
@Component
public class AcceptPartyInvitationService implements AcceptPartyInvitationUseCase {

    private final PartyInvitationTokenRepository partyInvitationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final Clock clock;

    public AcceptPartyInvitationService(PartyInvitationTokenRepository partyInvitationTokenRepository,
                                         UserRepository userRepository,
                                         PasswordEncoderPort passwordEncoderPort,
                                         Clock clock) {
        this.partyInvitationTokenRepository = partyInvitationTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserId accept(AcceptPartyInvitationCommand command) {
        PartyInvitationToken invitationToken = partyInvitationTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidPartyInvitationTokenException("Invalid invitation token"));
        if (invitationToken.isExpired(clock.instant())) {
            throw new InvalidPartyInvitationTokenException("Invitation token has expired");
        }

        User user = userRepository.findByEmail(invitationToken.email().value())
                .map(existing -> existing.withLinkedParty(invitationToken.partyId()))
                .orElseGet(() -> newAccountFor(invitationToken, command.password()));

        User saved = userRepository.save(user);
        partyInvitationTokenRepository.deleteByPartyId(invitationToken.partyId());
        return saved.getId();
    }

    private User newAccountFor(PartyInvitationToken invitationToken, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidPartyInvitationTokenException("A password is required to create an account");
        }
        HashedPassword hashedPassword = passwordEncoderPort.encode(RawPassword.of(rawPassword));
        return User.register(UserId.newId(), invitationToken.email(), invitationToken.fullName(), hashedPassword)
                .verify()
                .withLinkedParty(invitationToken.partyId());
    }
}
