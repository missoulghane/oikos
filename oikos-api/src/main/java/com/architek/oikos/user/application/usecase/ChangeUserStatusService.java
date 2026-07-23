package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.command.ChangeUserStatusCommand;
import com.architek.oikos.user.application.port.in.ChangeUserStatusUseCase;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public ChangeUserStatusService(UserRepository userRepository, PartyDirectoryPort partyDirectoryPort) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional
    public UserView changeStatus(ChangeUserStatusCommand command) {
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));
        User updated = userRepository.save(command.enabled() ? user.activate() : user.deactivate());
        return UserView.of(updated, partyDirectoryPort.getPartyById(updated.getPartyId()));
    }
}
