package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.GrantPropertyRoleCommand;
import com.architek.oikos.user.application.port.in.GrantPropertyRoleUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GrantPropertyRoleService implements GrantPropertyRoleUseCase {

    private final UserRepository userRepository;

    public GrantPropertyRoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void grant(GrantPropertyRoleCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        User updated = user.withLinkedParty(command.partyId())
                .withPropertyRoleGrant(command.partyId(), command.propertyId(), command.role());
        userRepository.save(updated);
    }
}
