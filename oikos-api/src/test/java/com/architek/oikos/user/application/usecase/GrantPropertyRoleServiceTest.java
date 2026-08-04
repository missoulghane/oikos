package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import com.architek.oikos.user.application.command.GrantPropertyRoleCommand;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.PropertyRoleGrant;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class GrantPropertyRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    private GrantPropertyRoleService newService() {
        return new GrantPropertyRoleService(userRepository);
    }

    @Test
    void granting_a_role_links_the_party_and_saves_the_grant() {
        UserId userId = UserId.newId();
        EntityId partyId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        User user = User.register(userId, EmailVO.of("jane.doe@example.com"), "Jane Doe", HashedPassword.of("hashed"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().grant(new GrantPropertyRoleCommand(userId, partyId, propertyId, PropertyRole.PROPERTY_OWNER));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getLinkedPartyIds()).contains(partyId);
        assertThat(captor.getValue().getPropertyRoleGrants())
                .contains(new PropertyRoleGrant(partyId, propertyId, PropertyRole.PROPERTY_OWNER));
    }

    @Test
    void granting_a_role_to_an_unknown_user_is_rejected() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().grant(
                new GrantPropertyRoleCommand(userId, EntityId.newId(), EntityId.newId(), PropertyRole.PROPERTY_OWNER)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
