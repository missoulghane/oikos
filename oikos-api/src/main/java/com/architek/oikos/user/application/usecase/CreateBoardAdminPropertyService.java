package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.CreateBoardAdminPropertyCommand;
import com.architek.oikos.user.application.port.in.CreateBoardAdminPropertyUseCase;
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.exception.BoardAdminPropertyAlreadyExistsException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Step 2 of the volunteer-syndic wizard, for an account created at step 1:
 * provisions the property (without buildings - laid out at the last step),
 * then a Party scoped to it, seats that Party on the board in the seat the
 * syndic chose, and grants the account PROPERTY_BOARD_ADMIN through it - in
 * that order, since the Party can only be created once the property id is
 * known. Whatever seat is chosen, the account administers the property: the
 * seat describes the person's office on the board, not their rights in the app.
 */
@Component
public class CreateBoardAdminPropertyService implements CreateBoardAdminPropertyUseCase {

    private final UserRepository userRepository;
    private final PartyProvisioningPort partyProvisioningPort;
    private final PropertyProvisioningPort propertyProvisioningPort;

    public CreateBoardAdminPropertyService(UserRepository userRepository,
                                            PartyProvisioningPort partyProvisioningPort,
                                            PropertyProvisioningPort propertyProvisioningPort) {
        this.userRepository = userRepository;
        this.partyProvisioningPort = partyProvisioningPort;
        this.propertyProvisioningPort = propertyProvisioningPort;
    }

    @Override
    @Transactional
    public EntityId create(CreateBoardAdminPropertyCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (user.getPropertyRoleGrants().stream().anyMatch(grant -> grant.role().isAdminTier())) {
            throw new BoardAdminPropertyAlreadyExistsException(user.getId());
        }

        EntityId propertyId = propertyProvisioningPort.provisionProperty(new PropertyProvisioningDetails(
                command.propertyName(), command.propertyAddress(), command.propertyCity()));
        EntityId partyId = partyProvisioningPort.createParty(
                new PartyProvisioningDetails(user.getFullName(), user.getEmail(), user.getPhone(), propertyId));
        propertyProvisioningPort.assignBoardMember(propertyId, partyId, command.boardSeat());

        userRepository.save(user.withLinkedParty(partyId)
                .withPropertyRoleGrant(partyId, propertyId, PropertyRole.PROPERTY_BOARD_ADMIN));
        return propertyId;
    }
}
