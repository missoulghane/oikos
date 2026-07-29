package com.architek.oikos.user.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.port.in.LoadUserByIdentifierUseCase;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Resolves a login identifier against the account's own email. A
 * non-matching identifier simply fails to match rather than erroring.
 */
@Component
public class LoadUserByIdentifierService implements LoadUserByIdentifierUseCase {

    private final UserRepository userRepository;

    public LoadUserByIdentifierService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> loadByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier);
    }
}
