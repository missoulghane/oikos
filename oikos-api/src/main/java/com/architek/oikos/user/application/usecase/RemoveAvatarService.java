package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.RemoveAvatarUseCase;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class RemoveAvatarService implements RemoveAvatarUseCase {

    private final UserRepository userRepository;

    public RemoveAvatarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserView removeAvatar(UserId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User updated = userRepository.save(user.withoutAvatar());
        return UserView.of(updated);
    }
}
