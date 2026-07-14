package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.port.in.OverwritePasswordUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class OverwritePasswordService implements OverwritePasswordUseCase {

    private final UserRepository userRepository;

    public OverwritePasswordService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void overwritePassword(UserId userId, HashedPassword newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.save(user.withPassword(newPassword));
    }
}
