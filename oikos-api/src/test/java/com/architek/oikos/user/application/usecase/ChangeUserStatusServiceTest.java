package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.ChangeUserStatusCommand;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class ChangeUserStatusServiceTest {

    @Mock
    private UserRepository userRepository;

    private ChangeUserStatusService newService() {
        return new ChangeUserStatusService(userRepository);
    }

    private static User newUser() {
        return User.register(UserId.newId(), EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
    }

    @Test
    void disabling_a_user_persists_the_deactivated_account() {
        User user = newUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = newService().changeStatus(new ChangeUserStatusCommand(user.getId(), false));

        assertThat(result.enabled()).isFalse();
    }

    @Test
    void enabling_a_user_persists_the_activated_account() {
        User user = newUser().deactivate();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = newService().changeStatus(new ChangeUserStatusCommand(user.getId(), true));

        assertThat(result.enabled()).isTrue();
    }

    @Test
    void changing_status_of_an_unknown_user_throws_not_found() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().changeStatus(new ChangeUserStatusCommand(id, true)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
