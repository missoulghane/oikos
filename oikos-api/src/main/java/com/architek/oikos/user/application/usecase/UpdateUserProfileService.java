package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.application.port.out.ContactDetails;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final ContactDirectoryPort contactDirectoryPort;

    public UpdateUserProfileService(UserRepository userRepository, ContactDirectoryPort contactDirectoryPort) {
        this.userRepository = userRepository;
        this.contactDirectoryPort = contactDirectoryPort;
    }

    @Override
    @Transactional
    public UserView updateProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        ContactDetails updated = contactDirectoryPort.updateContact(user.getContactId(),
                new ContactDetails(command.lastName(), command.firstName(), command.email(), command.phone()));
        return UserView.of(user, updated);
    }
}
