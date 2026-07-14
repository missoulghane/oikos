package com.architek.oikos.user.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.VerifyAccountCommand;
import com.architek.oikos.user.application.port.in.VerifyAccountUseCase;
import com.architek.oikos.user.domain.exception.InvalidVerificationTokenException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;

@Component
public class VerifyAccountService implements VerifyAccountUseCase {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public VerifyAccountService(VerificationTokenRepository verificationTokenRepository,
                                 UserRepository userRepository, Clock clock) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void verify(VerifyAccountCommand command) {
        VerificationToken token = verificationTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));
        if (token.isExpired(clock.instant())) {
            throw new InvalidVerificationTokenException("Verification token has expired");
        }
        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new UserNotFoundException(token.userId()));
        userRepository.save(user.verify());
        verificationTokenRepository.deleteByUserId(user.getId());
    }
}
