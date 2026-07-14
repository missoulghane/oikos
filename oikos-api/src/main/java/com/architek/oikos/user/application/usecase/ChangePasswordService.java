package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.ChangePasswordCommand;
import com.architek.oikos.user.application.port.in.ChangePasswordUseCase;
import com.architek.oikos.user.domain.exception.InvalidCurrentPasswordException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public ChangePasswordService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (!passwordEncoderPort.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }
        HashedPassword newHashedPassword = passwordEncoderPort.encode(command.newPassword());
        userRepository.save(user.withPassword(newHashedPassword));
    }
}
