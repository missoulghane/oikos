package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    public UpdateUserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserView updateProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (!user.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        User updated = userRepository.save(user.withEmail(command.email()).withFullName(command.fullName()));
        return UserView.of(updated);
    }
}
