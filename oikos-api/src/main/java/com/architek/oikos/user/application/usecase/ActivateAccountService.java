package com.architek.oikos.user.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.ActivateAccountCommand;
import com.architek.oikos.user.application.port.in.ActivateAccountUseCase;
import com.architek.oikos.user.domain.exception.InvalidVerificationTokenException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;

/**
 * Activates an admin-created account: same single-use token semantics as
 * VerifyAccountService, but additionally sets the password the user just chose
 * (admin-created accounts start with a random, never-disclosed placeholder).
 */
@Component
public class ActivateAccountService implements ActivateAccountUseCase {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final Clock clock;

    public ActivateAccountService(VerificationTokenRepository verificationTokenRepository,
                                   UserRepository userRepository,
                                   PasswordEncoderPort passwordEncoderPort,
                                   Clock clock) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void activate(ActivateAccountCommand command) {
        VerificationToken token = verificationTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));
        if (token.isExpired(clock.instant())) {
            throw new InvalidVerificationTokenException("Verification token has expired");
        }
        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new UserNotFoundException(token.userId()));
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.newPassword());
        userRepository.save(user.verify().withPassword(hashedPassword));
        verificationTokenRepository.deleteByUserId(user.getId());
    }
}
