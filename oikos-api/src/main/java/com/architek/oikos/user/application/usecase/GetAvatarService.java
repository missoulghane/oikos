package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;

import com.architek.oikos.user.application.dto.AvatarView;
import com.architek.oikos.user.application.port.in.GetAvatarUseCase;
import com.architek.oikos.user.domain.exception.AvatarNotFoundException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class GetAvatarService implements GetAvatarUseCase {

    private final UserRepository userRepository;

    public GetAvatarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AvatarView getAvatar(UserId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.hasAvatar()) {
            throw new AvatarNotFoundException(userId);
        }
        return new AvatarView(user.getAvatar(), user.getAvatarContentType());
    }
}
