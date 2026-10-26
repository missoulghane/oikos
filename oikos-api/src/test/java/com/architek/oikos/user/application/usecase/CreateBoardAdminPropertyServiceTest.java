package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.CreateBoardAdminPropertyCommand;
import com.architek.oikos.user.application.port.out.BoardSeat;
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.exception.BoardAdminPropertyAlreadyExistsException;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class CreateBoardAdminPropertyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyProvisioningPort partyProvisioningPort;

    @Mock
    private PropertyProvisioningPort propertyProvisioningPort;

    private CreateBoardAdminPropertyService newService() {
        return new CreateBoardAdminPropertyService(userRepository, partyProvisioningPort, propertyProvisioningPort);
    }

    private static User registeredAccount() {
        return User.register(UserId.newId(), EmailVO.of("board-admin@oikos.com"), "Jane Doe", "+212612345678",
                HashedPassword.of("hashed"), Role.ROLE_USER);
    }

    private static CreateBoardAdminPropertyCommand command(UserId userId, BoardSeat seat) {
        return new CreateBoardAdminPropertyCommand(userId, "Residence A", "1 rue de Paris", "Casablanca", seat);
    }

    @Test
    void creating_the_property_seats_the_account_party_in_the_chosen_seat_and_grants_board_admin() {
        User account = registeredAccount();
        EntityId propertyId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        when(userRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(propertyProvisioningPort.provisionProperty(any())).thenReturn(propertyId);
        when(partyProvisioningPort.createParty(any())).thenReturn(partyId);

        EntityId created = newService().create(command(account.getId(), BoardSeat.TREASURER));

        assertThat(created).isEqualTo(propertyId);

        ArgumentCaptor<PropertyProvisioningDetails> propertyCaptor =
                ArgumentCaptor.forClass(PropertyProvisioningDetails.class);
        verify(propertyProvisioningPort).provisionProperty(propertyCaptor.capture());
        assertThat(propertyCaptor.getValue().name()).isEqualTo("Residence A");
        assertThat(propertyCaptor.getValue().city()).isEqualTo("Casablanca");

        // The party mirrors the account typed at step 1, scoped to the new property.
        ArgumentCaptor<PartyProvisioningDetails> partyCaptor = ArgumentCaptor.forClass(PartyProvisioningDetails.class);
        verify(partyProvisioningPort).createParty(partyCaptor.capture());
        assertThat(partyCaptor.getValue().fullName()).isEqualTo("Jane Doe");
        assertThat(partyCaptor.getValue().phone()).isEqualTo("+212612345678");
        assertThat(partyCaptor.getValue().propertyId()).isEqualTo(propertyId);

        verify(propertyProvisioningPort).assignBoardMember(propertyId, partyId, BoardSeat.TREASURER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getLinkedPartyIds()).containsExactly(partyId);
        // Whatever the seat, the account administers the property it created.
        assertThat(userCaptor.getValue().getPropertyRoleGrants())
                .extracting("role").containsExactly(PropertyRole.PROPERTY_BOARD_ADMIN);
    }

    @Test
    void an_account_that_already_administers_a_property_cannot_create_a_second_one() {
        User account = registeredAccount()
                .withPropertyRoleGrant(EntityId.newId(), EntityId.newId(), PropertyRole.PROPERTY_BOARD_ADMIN);
        when(userRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> newService().create(command(account.getId(), BoardSeat.VOLUNTEER_MANAGER)))
                .isInstanceOf(BoardAdminPropertyAlreadyExistsException.class);

        verifyNoInteractions(propertyProvisioningPort, partyProvisioningPort);
        verify(userRepository, never()).save(any());
    }
}
