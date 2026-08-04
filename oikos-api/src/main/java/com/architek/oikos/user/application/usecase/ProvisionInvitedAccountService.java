package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.ProvisionInvitedAccountCommand;
import com.architek.oikos.user.application.port.in.ProvisionInvitedAccountUseCase;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Verifies the account immediately (like AcceptPartyInvitationService's
 * new-account branch), rather than issuing a verification email like plain
 * self-registration (RegisterUserService) - an invitation's whole point is
 * to let someone create an account and immediately continue (pick a unit,
 * submit a candidacy) in one sitting, which a "click the email we just sent
 * you" detour would break.
 */
@Component
public class ProvisionInvitedAccountService implements ProvisionInvitedAccountUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public ProvisionInvitedAccountService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public UserId provision(ProvisionInvitedAccountCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), hashedPassword).verify();
        return userRepository.save(user).getId();
    }
}
