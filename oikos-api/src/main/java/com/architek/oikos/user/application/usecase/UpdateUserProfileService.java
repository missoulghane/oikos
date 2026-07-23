package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.UpdateUserProfileUseCase;
import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class UpdateUserProfileService implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public UpdateUserProfileService(UserRepository userRepository, PartyDirectoryPort partyDirectoryPort) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional
    public UserView updateProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        PartyDetails updated = partyDirectoryPort.updateParty(user.getPartyId(),
                new PartyDetails(command.fullName(), command.email(), command.phone()));
        return UserView.of(user, updated);
    }
}
