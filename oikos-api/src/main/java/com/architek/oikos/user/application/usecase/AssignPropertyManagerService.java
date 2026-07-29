package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.AssignPropertyManagerCommand;
import com.architek.oikos.user.application.command.GrantCreatorAsManagerCommand;
import com.architek.oikos.user.application.port.in.AssignPropertyManagerUseCase;
import com.architek.oikos.user.application.port.in.GrantCreatorAsManagerUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class AssignPropertyManagerService implements AssignPropertyManagerUseCase {

    private final UserRepository userRepository;
    private final GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase;

    public AssignPropertyManagerService(UserRepository userRepository,
                                         GrantCreatorAsManagerUseCase grantCreatorAsManagerUseCase) {
        this.userRepository = userRepository;
        this.grantCreatorAsManagerUseCase = grantCreatorAsManagerUseCase;
    }

    @Override
    @Transactional
    public void assign(AssignPropertyManagerCommand command) {
        User user = userRepository.findByEmail(command.email().value())
                .orElseThrow(() -> new UserNotFoundException(
                        "No account found for email: " + command.email().value() + " - ask them to register first"));
        grantCreatorAsManagerUseCase.grant(
                new GrantCreatorAsManagerCommand(user.getId(), command.propertyId(), command.role()));
    }
}
