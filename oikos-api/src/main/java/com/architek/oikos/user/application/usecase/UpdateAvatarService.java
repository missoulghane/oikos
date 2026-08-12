package com.architek.oikos.user.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.UpdateAvatarCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.UpdateAvatarUseCase;
import com.architek.oikos.user.domain.exception.AvatarTooLargeException;
import com.architek.oikos.user.domain.exception.UnsupportedAvatarTypeException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class UpdateAvatarService implements UpdateAvatarUseCase {

    private final UserRepository userRepository;

    @Value("${oikos.avatar.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Value("#{'${oikos.avatar.allowed-content-types}'.split(',')}")
    private List<String> allowedContentTypes;

    public UpdateAvatarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserView updateAvatar(UpdateAvatarCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (command.content().length > maxFileSizeBytes) {
            throw new AvatarTooLargeException(command.content().length, maxFileSizeBytes);
        }
        if (!allowedContentTypes.contains(command.contentType())) {
            throw new UnsupportedAvatarTypeException(command.contentType());
        }
        User updated = userRepository.save(user.withAvatar(command.content(), command.contentType()));
        return UserView.of(updated);
    }
}
