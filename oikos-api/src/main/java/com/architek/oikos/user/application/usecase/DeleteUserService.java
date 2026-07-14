package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.DeleteUserCommand;
import com.architek.oikos.user.application.port.in.DeleteUserUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteUserCommand command) {
        if (userRepository.findById(command.userId()).isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }
        userRepository.deleteById(command.userId());
    }
}
