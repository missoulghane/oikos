package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.GrantCreatorAsManagerCommand;
import com.architek.oikos.user.application.port.in.GrantCreatorAsManagerUseCase;
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GrantCreatorAsManagerService implements GrantCreatorAsManagerUseCase {

    private final UserRepository userRepository;
    private final PartyProvisioningPort partyProvisioningPort;
    private final PropertyProvisioningPort propertyProvisioningPort;

    public GrantCreatorAsManagerService(UserRepository userRepository,
                                         PartyProvisioningPort partyProvisioningPort,
                                         PropertyProvisioningPort propertyProvisioningPort) {
        this.userRepository = userRepository;
        this.partyProvisioningPort = partyProvisioningPort;
        this.propertyProvisioningPort = propertyProvisioningPort;
    }

    @Override
    @Transactional
    public void grant(GrantCreatorAsManagerCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        EntityId partyId = partyProvisioningPort.createParty(
                new PartyProvisioningDetails(user.getFullName(), user.getEmail(), null, command.propertyId()));
        // Only ADMIN-tier grants (board admin / manager-firm admin) seat the party on the
        // property's board - a MEMBER-tier invitee gets the auth grant only, no board seat.
        if (command.role().isAdminTier()) {
            propertyProvisioningPort.assignPropertyManager(command.propertyId(), partyId);
        }

        User updated = user.withLinkedParty(partyId)
                .withPropertyRoleGrant(partyId, command.propertyId(), command.role());
        userRepository.save(updated);
    }
}
